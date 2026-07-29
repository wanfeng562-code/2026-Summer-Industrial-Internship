package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatRequest {

    /** 为空时自动创建新会话。 */
    @Size(max = 64, message = "会话编号不能超过64个字符")
    private String sessionNo;

    @NotBlank(message = "对话内容不能为空")
    @Size(max = 2000, message = "对话内容不能超过2000个字符")
    private String message;
}
