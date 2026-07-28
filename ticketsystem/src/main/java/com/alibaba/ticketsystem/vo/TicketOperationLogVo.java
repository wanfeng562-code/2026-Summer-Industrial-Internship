package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketOperationLogVo {

    private Long id;
    private Long ticketId;
    private String action;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String beforeStatus;
    private String afterStatus;
    private String detail;
    private LocalDateTime createTime;
}
