package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class DashboardStatsVo {

    private long total;
    private long aiProcessing;
    private long manualReview;
    private long resolved;
    private long rejected;
    private long closed;
    private long slaWarning;
    private long slaEscalated;
    private Map<String, Long> categoryCounts = new LinkedHashMap<>();
}
