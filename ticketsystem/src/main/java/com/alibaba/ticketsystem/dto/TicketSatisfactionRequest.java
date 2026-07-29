package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketSatisfactionRequest {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为 1")
    @Max(value = 5, message = "评分最高为 5")
    private Integer score;

    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String comment;
}
