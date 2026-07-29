package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.AiChatRequest;
import com.alibaba.ticketsystem.service.AIService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.AiChatResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
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

    @SaCheckPermission("ai:chat")
    @PostMapping("/chat")
    public R<?> chat(@Valid @RequestBody AiChatRequest request) {
        return R.success("AI回复成功", new AiChatResponse(aiService.chat(request.getMessage())));
    }

    @SaCheckPermission("ai:chat")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<ServerSentEvent<String>> stream(@Valid @RequestBody AiChatRequest request,
                                                HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return aiService.streamChat(request.getMessage())
                .map(chunk -> ServerSentEvent.builder(chunk).event("message").build())
                .concatWithValues(ServerSentEvent.builder("[DONE]").event("done").build())
                .onErrorResume(exception -> {
                    log.error("[AI] SSE stream failed", exception);
                    return Flux.just(ServerSentEvent.builder("AI服务暂时不可用，请稍后重试")
                            .event("error")
                            .build());
                });
    }
}
