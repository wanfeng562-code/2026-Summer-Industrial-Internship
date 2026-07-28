package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketSlaServiceTest {

    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private TicketOperationLogService logService;

    @Test
    @SuppressWarnings("unchecked")
    void overdueTicketIsWarnedAndEscalated() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 28, 12, 0);
        Ticket ticket = ticket(now.minusHours(49), now.minusHours(1), "MEDIUM");
        when(ticketMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(ticket));
        TicketSlaService service = new TicketSlaService(ticketMapper, logService);

        int changed = service.processDueTickets(now);

        assertThat(changed).isEqualTo(1);
        assertThat(ticket.getSlaWarning()).isEqualTo(1);
        assertThat(ticket.getSlaEscalated()).isEqualTo(1);
        assertThat(ticket.getPriority()).isEqualTo("HIGH");
        verify(ticketMapper).updateById(ticket);
        verify(logService).record(1L, "SLA_WARNING", null, "SYSTEM",
                "MANUAL_REVIEW", "MANUAL_REVIEW", "SLA剩余时间不足总时长的25%");
        verify(logService).record(1L, "SLA_ESCALATE", null, "SYSTEM",
                "MANUAL_REVIEW", "MANUAL_REVIEW", "SLA已超时，优先级由MEDIUM升级为HIGH");
    }

    @Test
    @SuppressWarnings("unchecked")
    void ticketBeforeWarningPointIsUnchanged() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 28, 12, 0);
        Ticket ticket = ticket(now.minusHours(1), now.plusHours(47), "MEDIUM");
        when(ticketMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(ticket));
        TicketSlaService service = new TicketSlaService(ticketMapper, logService);

        assertThat(service.processDueTickets(now)).isZero();
        verify(ticketMapper, never()).updateById(any(Ticket.class));
        verifyNoInteractions(logService);
    }

    private Ticket ticket(LocalDateTime createTime, LocalDateTime deadline, String priority) {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus("MANUAL_REVIEW");
        ticket.setPriority(priority);
        ticket.setSlaWarning(0);
        ticket.setSlaEscalated(0);
        ticket.setCreateTime(createTime);
        ticket.setSlaDeadline(deadline);
        ticket.setDeleted(0);
        return ticket;
    }
}
