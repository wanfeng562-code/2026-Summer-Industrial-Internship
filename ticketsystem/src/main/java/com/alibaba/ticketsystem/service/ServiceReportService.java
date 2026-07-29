package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.domain.TicketStatus;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketOperationLog;
import com.alibaba.ticketsystem.entity.TicketSatisfaction;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.AgentGroup;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.mapper.TicketOperationLogMapper;
import com.alibaba.ticketsystem.mapper.TicketSatisfactionMapper;
import com.alibaba.ticketsystem.vo.ServiceReportVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.alibaba.ticketsystem.utils.ApiException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceReportService {
    private final TicketMapper ticketMapper;
    private final TicketOperationLogMapper logMapper;
    private final TicketSatisfactionMapper satisfactionMapper;
    private final UserService userService;
    private final AgentGroupService groupService;

    public ServiceReportVo report(Integer year, Integer month) {
        if ((year == null) != (month == null) || (month != null && (month < 1 || month > 12))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "year 和 month 必须同时提供，month 范围为 1-12");
        }
        if (year != null && (year < 2000 || year > 2100)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "year 范围为 2000-2100");
        }
        SysUser currentUser = userService.requireCurrentUser();
        LocalDateTime from = null;
        LocalDateTime to = null;
        if (year != null && month != null) {
            LocalDate first = LocalDate.of(year, month, 1);
            from = first.atStartOfDay();
            to = first.plusMonths(1).atStartOfDay();
        }
        final LocalDateTime rangeFrom = from;
        final LocalDateTime rangeTo = to;
        List<Ticket> tickets = ticketMapper.selectList(new QueryWrapper<Ticket>().eq("deleted", 0)).stream()
                .filter(ticket -> inRange(ticket.getCreateTime(), rangeFrom, rangeTo)).toList();
        if ("AGENT".equals(currentUser.getRole())) {
            if (currentUser.getAgentGroupId() == null) {
                throw new ApiException(HttpStatus.FORBIDDEN, "当前客服未加入坐席组，不能查看组级报表");
            }
            AgentGroup group = groupService.requireActive(currentUser.getAgentGroupId());
            if (!currentUser.getId().equals(group.getLeaderId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "仅坐席组长可以查看组级报表");
            }
            tickets = tickets.stream().filter(ticket -> group.getId().equals(ticket.getGroupId())).toList();
        } else if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "仅管理员或坐席组长可以查看服务报表");
        }
        Set<Long> ticketIds = tickets.stream().map(Ticket::getId).collect(Collectors.toSet());
        List<TicketOperationLog> logs = logMapper.selectList(new QueryWrapper<TicketOperationLog>()).stream()
                .filter(log -> ticketIds.contains(log.getTicketId()))
                .filter(log -> inRange(log.getCreateTime(), rangeFrom, rangeTo)).toList();
        List<TicketSatisfaction> satisfactions = satisfactionMapper.selectList(new QueryWrapper<TicketSatisfaction>()).stream()
                .filter(item -> ticketIds.contains(item.getTicketId()))
                .filter(item -> inRange(item.getCreateTime(), rangeFrom, rangeTo)).toList();

        ServiceReportVo result = new ServiceReportVo();
        result.setTicketCount(tickets.size());
        result.setReceptionCount(logs.stream().filter(log -> "CLAIM".equals(log.getAction())).count());
        result.setAiReplyCount(logs.stream().filter(log -> "AI_REPLY".equals(log.getAction())).count());
        result.setTransferToHumanCount(logs.stream().filter(log -> "TRANSFER_MANUAL".equals(log.getAction())
                || "AI_TRANSFER_MANUAL".equals(log.getAction())).count());
        result.setCompletedCount(tickets.stream().filter(ticket -> TicketStatus.CLOSED.name().equals(ticket.getStatus())).count());
        result.setSatisfactionCount(satisfactions.size());
        result.setAiReplyRate(rate(result.getAiReplyCount(), result.getTicketCount()));
        result.setTransferToHumanRate(rate(result.getTransferToHumanCount(), result.getTicketCount()));
        result.setCompletionRate(rate(result.getCompletedCount(), result.getTicketCount()));
        result.setAverageSatisfaction(satisfactions.isEmpty() ? BigDecimal.ZERO :
                satisfactions.stream().map(item -> BigDecimal.valueOf(item.getScore()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(satisfactions.size()), 2, RoundingMode.HALF_UP));
        Map<Long, Long> agentCounts = new LinkedHashMap<>();
        logs.stream().filter(log -> "CLAIM".equals(log.getAction()) && log.getOperatorId() != null)
                .forEach(log -> agentCounts.merge(log.getOperatorId(), 1L, Long::sum));
        result.setAgentReceptionCounts(agentCounts);
        return result;
    }

    private boolean inRange(LocalDateTime value, LocalDateTime from, LocalDateTime to) {
        return value != null && (from == null || !value.isBefore(from)) && (to == null || value.isBefore(to));
    }

    private BigDecimal rate(long numerator, long denominator) {
        return denominator == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
