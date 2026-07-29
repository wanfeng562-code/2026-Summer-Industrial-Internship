package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TicketPriorityRequest {
    @NotBlank(message = "优先级不能为空")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "优先级不正确")
    private String priority;
}
