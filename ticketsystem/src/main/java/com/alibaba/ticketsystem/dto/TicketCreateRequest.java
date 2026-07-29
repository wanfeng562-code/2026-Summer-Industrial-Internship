package com.alibaba.ticketsystem.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCreateRequest {

    @NotNull(message = "订单ID不能为空")
    @JsonAlias("ordersId")
    private Long orderId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @NotBlank(message = "工单描述不能为空")
    @Size(max = 5000, message = "工单描述不能超过5000个字符")
    private String description;

    @Size(max = 30, message = "工单分类不能超过30个字符")
    private String category;

    @Pattern(regexp = "^$|LOW|MEDIUM|HIGH|URGENT$", message = "工单优先级不正确")
    private String priority;
}
