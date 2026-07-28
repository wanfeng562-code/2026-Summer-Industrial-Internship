package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 售后策略表
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@Getter
@Setter
@TableName("after_sale_policy")
public class AfterSalePolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 策略ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 策略名称
     */
    private String policyName;

    /**
     * 适用分类: REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER
     */
    private String category;

    /**
     * 条件类型: ALWAYS/AMOUNT_REPUTATION/AMOUNT/REPUTATION
     */
    private String conditionType;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;

    /**
     * 最大金额
     */
    private BigDecimal maxAmount;

    /**
     * 最低信誉分
     */
    private Integer minReputation;

    /**
     * 处理动作: AUTO_APPROVE/AUTO_REPLY/MANUAL
     */
    private String action;

    /**
     * 回复模板
     */
    private String replyTemplate;

    /**
     * 优先级(越小越优先)
     */
    private Integer priority;

    /**
     * 启用状态: 0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 命中该策略时的 SLA 时长（小时），为空时使用优先级默认值。
     */
    private Integer slaHours;

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
