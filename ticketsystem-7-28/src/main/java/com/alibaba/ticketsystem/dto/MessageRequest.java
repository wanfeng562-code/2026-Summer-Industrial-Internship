package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {

    @NotBlank(message = "工单ID不能为空")
    private Long ticketId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

}
