package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.AiChatRequest;
import com.alibaba.ticketsystem.service.AIService;
import com.alibaba.ticketsystem.service.ContentModerationService;
import com.alibaba.ticketsystem.service.AiChatHistoryService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.AiChatResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/ai")
public class AiChatController {

    private final AIService aiService;
    private final ContentModerationService contentModerationService;
    private final AiChatHistoryService historyService;

    @SaCheckPermission("ai:chat")
    @PostMapping("/chat")
    public R<?> chat(@Valid @RequestBody AiChatRequest request) {
        contentModerationService.validateUserContent(request.getMessage());
        var session = historyService.recordUserMessage(request.getSessionNo(), request.getMessage());
        String answer = aiService.chat(historyService.buildModelPrompt(session.getSessionNo()));
        historyService.recordAssistantMessage(session.getId(), answer);
        return R.success("AI回复成功", new AiChatResponse(answer, session.getSessionNo()));
    }

    @SaCheckPermission("ai:chat")
    @GetMapping("/chat/sessions")
    public R<?> sessions() {
        return R.success("AI会话历史查询成功", historyService.sessions());
    }

    @SaCheckPermission("ai:chat")
    @GetMapping("/chat/sessions/{sessionNo}/messages")
    public R<?> messages(@PathVariable @Size(max = 64) String sessionNo) {
        return R.success("AI会话消息查询成功", historyService.messages(sessionNo));
    }

    @SaCheckPermission("ai:chat")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<ServerSentEvent<String>> stream(@Valid @RequestBody AiChatRequest request,
                                                HttpServletResponse response) {
        contentModerationService.validateUserContent(request.getMessage());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        var session = historyService.recordUserMessage(request.getSessionNo(), request.getMessage());
        String modelPrompt = historyService.buildModelPrompt(session.getSessionNo());
        StringBuilder answer = new StringBuilder();
        Flux<ServerSentEvent<String>> responseFrames = aiService.streamChat(modelPrompt)
                .doOnNext(answer::append)
                .doOnComplete(() -> historyService.recordAssistantMessage(session.getId(), answer.toString()))
                .map(chunk -> ServerSentEvent.builder(chunk).event("message").build())
                .concatWithValues(ServerSentEvent.builder("[DONE]").event("done").build())
                .onErrorResume(exception -> {
                    log.error("[AI] SSE stream failed", exception);
                    return Flux.just(ServerSentEvent.builder("AI服务暂时不可用，请稍后重试")
                            .event("error")
                            .build());
                });
        return Flux.concat(
                Flux.just(ServerSentEvent.builder(session.getSessionNo()).event("session").build()),
                responseFrames);
    }
}
