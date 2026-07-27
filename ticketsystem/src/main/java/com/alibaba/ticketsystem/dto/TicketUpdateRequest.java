package com.alibaba.ticketsystem.dto;

import lombok.Data;

@Data
public class TicketUpdateRequest {

    private String status;

    private String category;

    private String priority;

    private Long agentId;

}
