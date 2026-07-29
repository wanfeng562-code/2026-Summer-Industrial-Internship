package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.domain.TicketStatus;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.vo.DashboardStatsVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private static final List<String> CATEGORIES =
            List.of("REFUND", "LOGISTICS", "DAMAGE", "INVOICE", "OTHER");

    private final TicketMapper ticketMapper;
    private final UserService userService;

    public DashboardStatsVo currentScopeStats() {
        SysUser currentUser = userService.requireCurrentUser();
        List<Ticket> tickets = ticketMapper.selectList(scopeQuery(currentUser));

        DashboardStatsVo result = new DashboardStatsVo();
        result.setTotal(tickets.size());
        result.setAiProcessing(countStatus(tickets, TicketStatus.AI_PROCESSING));
        result.setManualReview(countStatus(tickets, TicketStatus.MANUAL_REVIEW));
        result.setResolved(countStatus(tickets, TicketStatus.RESOLVED));
        result.setRejected(countStatus(tickets, TicketStatus.REJECTED));
        result.setClosed(countStatus(tickets, TicketStatus.CLOSED));
        result.setSlaWarning(tickets.stream()
                .filter(ticket -> Integer.valueOf(1).equals(ticket.getSlaWarning())).count());
        result.setSlaEscalated(tickets.stream()
                .filter(ticket -> Integer.valueOf(1).equals(ticket.getSlaEscalated())).count());

        Map<String, Long> categories = new LinkedHashMap<>();
        for (String category : CATEGORIES) {
            categories.put(category, 0L);
        }
        for (Ticket ticket : tickets) {
            if (ticket.getCategory() != null) {
                categories.merge(ticket.getCategory(), 1L, Long::sum);
            }
        }
        result.setCategoryCounts(categories);
        return result;
    }

    private QueryWrapper<Ticket> scopeQuery(SysUser user) {
        QueryWrapper<Ticket> query = new QueryWrapper<Ticket>().eq("deleted", 0);
        return switch (user.getRole()) {
            case "USER" -> query.eq("user_id", user.getId());
            case "AGENT" -> query.and(wrapper -> {
                wrapper.eq("agent_id", user.getId())
                        .or(nested -> nested.isNull("agent_id")
                                .isNull("group_id")
                                .eq("status", TicketStatus.MANUAL_REVIEW.name()));
                if (user.getAgentGroupId() != null) {
                    wrapper.or().eq("group_id", user.getAgentGroupId());
                }
            });
            case "ADMIN" -> query;
            default -> query.eq("id", -1);
        };
    }

    private long countStatus(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream().filter(ticket -> status.name().equals(ticket.getStatus())).count();
    }
}
