package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.vo.DashboardStatsVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardStatsServiceTest {

    @Test
    void aggregatesAccessibleTicketsByStatusCategoryAndSla() {
        TicketMapper ticketMapper = mock(TicketMapper.class);
        UserService userService = mock(UserService.class);
        DashboardStatsService service = new DashboardStatsService(ticketMapper, userService);
        SysUser user = new SysUser();
        user.setId(4L);
        user.setRole("USER");
        when(userService.requireCurrentUser()).thenReturn(user);
        when(ticketMapper.selectList(any())).thenReturn(List.of(
                ticket("AI_PROCESSING", "LOGISTICS", 1, 0),
                ticket("MANUAL_REVIEW", "REFUND", 0, 1),
                ticket("RESOLVED", "REFUND", 0, 0),
                ticket("CLOSED", "OTHER", 0, 0),
                ticket("CLOSED", "QUALITY", 0, 0)
        ));

        DashboardStatsVo result = service.currentScopeStats();

        assertThat(result.getTotal()).isEqualTo(5);
        assertThat(result.getAiProcessing()).isEqualTo(1);
        assertThat(result.getManualReview()).isEqualTo(1);
        assertThat(result.getResolved()).isEqualTo(1);
        assertThat(result.getClosed()).isEqualTo(2);
        assertThat(result.getSlaWarning()).isEqualTo(1);
        assertThat(result.getSlaEscalated()).isEqualTo(1);
        assertThat(result.getCategoryCounts()).containsEntry("REFUND", 2L)
                .containsEntry("LOGISTICS", 1L)
                .containsEntry("DAMAGE", 0L)
                .containsEntry("QUALITY", 1L);
    }

    private Ticket ticket(String status, String category, int warning, int escalated) {
        Ticket ticket = new Ticket();
        ticket.setStatus(status);
        ticket.setCategory(category);
        ticket.setSlaWarning(warning);
        ticket.setSlaEscalated(escalated);
        return ticket;
    }
}
