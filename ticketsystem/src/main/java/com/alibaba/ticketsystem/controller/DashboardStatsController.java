package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.service.DashboardStatsService;
import com.alibaba.ticketsystem.service.ServiceReportService;
import com.alibaba.ticketsystem.utils.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class DashboardStatsController {

    private final DashboardStatsService statsService;
    private final ServiceReportService reportService;

    @GetMapping("/service")
    @SaCheckPermission("stats:query")
    public R<?> serviceStats(@RequestParam(required = false) Integer year,
                             @RequestParam(required = false) Integer month) {
        return R.success("服务运营统计查询成功", reportService.report(year, month));
    }

    @GetMapping("/service/export")
    @SaCheckPermission("report:export")
    public void exportServiceReport(@RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) Integer month,
                                    HttpServletResponse response) throws IOException {
        var report = reportService.report(year, month);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=service-report.csv");
        response.getWriter().write('\ufeff');
        response.getWriter().write("指标,数值\n");
        response.getWriter().write("工单总数," + report.getTicketCount() + "\n");
        response.getWriter().write("接待量," + report.getReceptionCount() + "\n");
        response.getWriter().write("AI自动回复率," + report.getAiReplyRate() + "%\n");
        response.getWriter().write("转人工率," + report.getTransferToHumanRate() + "%\n");
        response.getWriter().write("工单完结率," + report.getCompletionRate() + "%\n");
        response.getWriter().write("平均满意度," + report.getAverageSatisfaction() + "\n");
    }

    @GetMapping("/tickets")
    @SaCheckPermission("ticket:query")
    public R<?> ticketStats() {
        return R.success("工作台统计查询成功", statsService.currentScopeStats());
    }
}
