package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.service.DashboardStatsService;
import com.alibaba.ticketsystem.utils.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class DashboardStatsController {

    private final DashboardStatsService statsService;

    @GetMapping("/tickets")
    @SaCheckPermission("ticket:query")
    public R<?> ticketStats() {
        return R.success("工作台统计查询成功", statsService.currentScopeStats());
    }
}
