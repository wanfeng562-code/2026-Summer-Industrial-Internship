package com.alibaba.springaidemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatAiController {

    @Autowired
    @Qualifier("default")
    private ChatClient chatClient;

    @Autowired
    @Qualifier("teacher")
    private ChatClient teacherClient;

    @Autowired
    @Qualifier("promt")
    private ChatClient promtClient;

    @GetMapping("/chat/ai")
    public String generation(@RequestParam(value = "userInput", defaultValue = "你是谁？") String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .call().content();
    }

    //http://localhost:8080/teacher/ai?userInput=长沙有多少个名字叫燕老师
    //http://localhost:8080/chat/ai?userInput=长沙有多少个名字叫燕老师
    @GetMapping("/teacher/ai")
    public String teacher(@RequestParam(value = "userInput", defaultValue = "你是谁？") String userInput) {
        return this.teacherClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping(value = "/stream/ai" ,produces = "text/stream;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam(value = "userInput",
            defaultValue = "你是谁？") String userInput){
        return this.teacherClient.prompt()
                .user(userInput).stream().content();
    }

    @GetMapping("/promt/ai")
    public String promtTemplate(@RequestParam(value = "userInput", defaultValue = "请帮我生成一个'Linux 助手'的提示词") String userInput) {
        return this.promtClient.prompt()
                .user(userInput)
                .call()
                .content();
    }
}
