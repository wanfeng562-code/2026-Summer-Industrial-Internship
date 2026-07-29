package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket_satisfaction")
public class TicketSatisfaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ticketId;
    private Long userId;
    private Integer score;
    private String comment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
