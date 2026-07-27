package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 工单消息表
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@Getter
@Setter
@TableName("ticket_message")
public class TicketMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 发送者用户ID
     */
    private Long userId;

    /**
     * 发送者类型: USER/AGENT/AI/SYSTEM
     */
    private String senderType;

    /**
     * 消息类型: TEXT/AI_REPLY/AI_SUGGESTION/SYSTEM
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * AI处理结果JSON
     */
    private String aiProcessResult;

    /**
     * 人工反馈
     */
    private String humanFeedback;

    /**
     * 逻辑删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
