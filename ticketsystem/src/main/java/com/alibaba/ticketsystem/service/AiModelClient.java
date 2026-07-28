package com.alibaba.ticketsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class AiModelClient {

    private final ChatClient chatClient;

    public String generate(String prompt) {
        return chatClient.prompt().user(prompt).call().content();
    }

    public Flux<String> stream(String prompt) {
        return chatClient.prompt().user(prompt).stream().content();
    }
}
