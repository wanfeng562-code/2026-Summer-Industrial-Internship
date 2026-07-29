package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRejectRequest {
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 1000, message = "驳回原因不能超过1000个字符")
    private String reason;
}
