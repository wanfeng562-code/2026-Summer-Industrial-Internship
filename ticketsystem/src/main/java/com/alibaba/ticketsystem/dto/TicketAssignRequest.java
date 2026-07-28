package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketAssignRequest {

    @NotNull(message = "客服ID不能为空")
    private Long agentId;
}
