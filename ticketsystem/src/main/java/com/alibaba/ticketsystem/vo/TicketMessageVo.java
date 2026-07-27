package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketMessageVo {

    private Long id;
    private Long ticketId;
    private Long userId;
    private String senderName;
    private String senderType;
    private String messageType;
    private String content;
    private String aiProcessResult;
    private String humanFeedback;
    private LocalDateTime createTime;

}
