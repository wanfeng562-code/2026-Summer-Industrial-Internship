package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket_operation_log")
public class TicketOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ticketId;
    private String action;
    private Long operatorId;
    private String operatorRole;
    private String beforeStatus;
    private String afterStatus;
    private String detail;
    private LocalDateTime createTime;
}
