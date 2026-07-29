package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("faq_semantic_config")
public class FaqSemanticConfig {
    @TableId
    private Long id;
    private Integer enabled;
    private BigDecimal similarityThreshold;
    private Integer maxCandidates;
    private Integer maxResults;
    private LocalDateTime updateTime;
}
