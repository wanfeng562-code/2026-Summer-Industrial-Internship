package com.alibaba.springaidemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class PromtController {

    @Autowired
    @Qualifier("teacher")
    private ChatClient chatClient;

    //http://localhost:8080/template/ai?name=燕老师&voice=湖北
    @GetMapping(value = "/template/ai", produces = "text/stream;charset=utf-8")
    public Flux<String> prompt(@RequestParam("name") String name,
                               @RequestParam("voice") String voice){
        String userText= """
            给我推荐武汉的至少三种美食
            """;
        UserMessage userMessage = new UserMessage(userText);
        String systemText= """
            你是一个美食咨询助手，可以帮助人们查询美食信息。
            你的名字是{name},
            你应该用你的名字和{voice}的饮食习惯回复用户的请求。
            """;
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        //替换占位符
        Message systemMessage = systemPromptTemplate
                .createMessage(Map.of("name", name, "voice", voice));
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
        return chatClient.prompt(prompt).stream().content();
    }
}
