package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 工单表
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@Getter
@Setter
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工单编号
     */
    private String ticketNo;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 处理客服ID
     */
    private Long agentId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 工单描述
     */
    private String description;

    /**
     * 分类: REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER
     */
    private String category;

    /**
     * 状态: PENDING/AI_PROCESSING/MANUAL_REVIEW/RESOLVED/CLOSED
     */
    private String status;

    /**
     * 优先级: LOW/MEDIUM/HIGH/URGENT
     */
    private String priority;

    /**
     * SLA预警: 0-正常 1-已预警
     */
    private Integer slaWarning;

    /**
     * SLA升级: 0-正常 1-已升级
     */
    private Integer slaEscalated;

    /**
     * SLA截止时间
     */
    private LocalDateTime slaDeadline;

    /**
     * 解决时间
     */
    private LocalDateTime resolveTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 逻辑删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
