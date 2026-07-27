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
 * AI处理日志表
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@Getter
@Setter
@TableName("ai_process_log")
public class AiProcessLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 意图识别结果
     */
    private String intentResult;

    /**
     * 匹配到的策略
     */
    private String policyMatched;

    /**
     * AI执行动作
     */
    private String aiAction;

    /**
     * AI生成回复
     */
    private String aiReply;

    /**
     * 处理详情
     */
    private String processDetail;

    /**
     * 执行耗时(ms)
     */
    private Integer executionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
