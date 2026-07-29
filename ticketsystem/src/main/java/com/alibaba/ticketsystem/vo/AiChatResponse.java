package com.alibaba.ticketsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiChatResponse {

    private String content;
    private String sessionNo;

    public AiChatResponse(String content) {
        this.content = content;
    }
}
