package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ServiceReportVo {
    private long ticketCount;
    private long receptionCount;
    private long aiReplyCount;
    private long transferToHumanCount;
    private long completedCount;
    private BigDecimal aiReplyRate = BigDecimal.ZERO;
    private BigDecimal transferToHumanRate = BigDecimal.ZERO;
    private BigDecimal completionRate = BigDecimal.ZERO;
    private BigDecimal averageSatisfaction = BigDecimal.ZERO;
    private long satisfactionCount;
    private Map<Long, Long> agentReceptionCounts = new LinkedHashMap<>();
}
