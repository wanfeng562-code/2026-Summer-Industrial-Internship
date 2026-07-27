package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketVo {

    private Long id;
    private String ticketNo;
    private Long userId;
    private String username;
    private String userNickname;
    private Long agentId;
    private String agentName;
    private Long orderId;
    private String orderNo;
    private String title;
    private String description;
    private String category;
    private String categoryName;
    private String status;
    private String statusName;
    private String priority;
    private Integer slaWarning;
    private Integer slaEscalated;
    private LocalDateTime slaDeadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<TicketMessageVo> messages;

}
