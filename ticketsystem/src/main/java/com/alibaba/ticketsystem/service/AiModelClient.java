package com.alibaba.ticketsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiModelClient {

    private final ChatClient chatClient;

    public String generate(String prompt) {
        return chatClient.prompt().user(prompt).call().content();
    }

    public String generate(String prompt, Long currentUserId) {
        return chatClient.prompt()
                .user(prompt)
                .toolContext(Map.of("currentUserId", currentUserId))
                .call()
                .content();
    }

    public Flux<String> stream(String prompt) {
        return chatClient.prompt().user(prompt).stream().content();
    }

    public Flux<String> stream(String prompt, Long currentUserId) {
        return chatClient.prompt()
                .user(prompt)
                .toolContext(Map.of("currentUserId", currentUserId))
                .stream()
                .content();
    }
}
