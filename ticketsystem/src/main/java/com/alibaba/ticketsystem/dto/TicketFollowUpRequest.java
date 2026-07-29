package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketFollowUpRequest {
    @NotBlank(message = "跟进内容不能为空")
    @Size(max = 2000, message = "跟进内容不能超过2000个字符")
    private String content;
}
