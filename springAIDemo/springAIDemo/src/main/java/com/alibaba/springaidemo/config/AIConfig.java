package com.alibaba.springaidemo.config;

import com.alibaba.springaidemo.tools.NameCountTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置不同的角色， 配置不同的大模型
 */
@Configuration
public class AIConfig {

    //默认把ChatClient放入容器中，配置ChatClient
    @Bean("default")
    public ChatClient getChatClient(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder.build();
    }


    //指定角色
    @Bean("teacher")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder.defaultSystem("你是一名大学计算机专业老师，" +
                "精通java、web、python、鸿蒙开发").defaultTools(nameCountTools()).build();
    }

    //指定角色
    @Bean("promt")
    public ChatClient chatPromt(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder.defaultSystem("你是一位大模型提示词生成专家，请根据用户的需求编写一个智能助手的提示词，来指导大模型进行内容生成，要求：\n" +
                "1. 以字符串的形式输出\n" +
                "2. 贴合用户需求，描述智能助手的定位、能力、知识储备\n" +
                "3. 提示词应清晰、精确、易于理解，在保持质量的同时，尽可能简洁\n" +
                "4. 只输出提示词，不要输出多余解释").build();
    }

    @Bean
    public NameCountTools nameCountTools(){
        return new NameCountTools();
    }
}
