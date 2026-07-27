package com.alibaba.springaidemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeepSeekController {

//    @Autowired
//    private ChatClient chatClient;

    //通过构造函数创建chatClient实例
//    public DeepSeekController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }

/**
 * 处理AI对话请求的接口方法
 * 当访问"/ai"路径时触发此方法
 *
 * @param userInput 用户输入的查询内容，默认值为"你是谁？"
 * @return 返回AI生成的对话内容
 */
//    @GetMapping("/ai")
//    public String generation(@RequestParam(value = "userInput", defaultValue = "你是谁？") String userInput) {
//    // 使用chatClient处理用户输入并获取AI响应
//    // 1. 创建新的prompt对话
//    // 2. 设置用户输入内容
//    // 3. 调用AI生成对话内容
//    // 4. 返回AI生成的响应内容
//        return this.chatClient.prompt()
//                .user(userInput)
//                .call()
//                .content();
//    }

}
