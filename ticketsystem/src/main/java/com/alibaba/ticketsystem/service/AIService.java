package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.ticketsystem.utils.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private static final Set<String> CATEGORIES =
            Set.of("REFUND", "LOGISTICS", "DAMAGE", "INVOICE", "OTHER");

    private final AiModelClient modelClient;
    private final TicketAiContextService contextService;
    private final UserService userService;

    public String chat(String message) {
        SysUser currentUser = userService.requireCurrentUser();
        return callAI(message, currentUser.getId());
    }

    public Flux<String> streamChat(String message) {
        SysUser currentUser = userService.requireCurrentUser();
        log.info("[AI] stream request promptLength={}", message.length());
        return modelClient.stream(message, currentUser.getId())
                .timeout(Duration.ofSeconds(60))
                .filter(chunk -> chunk != null && !chunk.isEmpty())
                .onErrorMap(exception -> {
                    log.error("[AI] stream request failed: {}: {}",
                            exception.getClass().getSimpleName(), exception.getMessage(), exception);
                    return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                            "AI服务暂时不可用，请稍后重试");
                })
                .doOnComplete(() -> log.info("[AI] stream response completed"));
    }

    public String callAI(String prompt) {
        return callAI(prompt, null);
    }

    private String callAI(String prompt, Long currentUserId) {
        log.info("[AI] request promptLength={}", prompt.length());
        try {
            String result = currentUserId == null
                    ? modelClient.generate(prompt)
                    : modelClient.generate(prompt, currentUserId);
            if (result == null || result.isBlank()) {
                throw new IllegalStateException("模型返回内容为空");
            }
            log.info("[AI] response responseLength={}", result.length());
            return result;
        } catch (Exception exception) {
            log.warn("[AI] model request failed: {}", exception.getClass().getSimpleName());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI服务暂时不可用，请稍后重试");
        }
    }

    public String classify(String description) {
        String prompt = """
                请将用户售后问题归为以下一个类别：
                REFUND、LOGISTICS、DAMAGE、INVOICE、OTHER。
                只返回一个英文类别代码，不要解释。
                用户问题：""" + description;
        try {
            String result = callAI(prompt).trim().toUpperCase(Locale.ROOT);
            return CATEGORIES.contains(result) ? result : "OTHER";
        } catch (Exception exception) {
            log.warn("[AI] classification failed, fallback to OTHER: {}", exception.getMessage());
            return "OTHER";
        }
    }

    public String processTicket(Long ticketId, String description, Long userId) {
        String businessContext = contextService.buildOwnedContext(ticketId, userId);
        String prompt = String.format("""
                请以电商客服身份回复下面的工单消息。
                你只能提供解释、建议和下一步操作指引，不得声称已经退款、补偿、修改订单或关闭工单。
                无法确认事实或需要业务写入时，应明确建议转人工处理。

                已经后端权限校验的业务上下文：
                %s

                用户消息：%s
                """, businessContext, description);
        return callAI(prompt, userId);
    }
}
