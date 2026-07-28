package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCreateRequest {

    @NotNull(message = "订单ID不能为空")
    private Long ordersId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "工单描述不能为空")
    private String description;

    private String category;
}
