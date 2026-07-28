package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.domain.TicketPriority;
import com.alibaba.ticketsystem.domain.TicketStatus;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSlaService {

    private final TicketMapper ticketMapper;
    private final TicketOperationLogService operationLogService;

    @Scheduled(fixedDelayString = "${ticket.sla.scan-interval-ms:60000}")
    public void scan() {
        processDueTickets(LocalDateTime.now());
    }

    @Transactional
    public int processDueTickets(LocalDateTime now) {
        List<Ticket> tickets = ticketMapper.selectList(new QueryWrapper<Ticket>()
                .eq("deleted", 0)
                .in("status", TicketStatus.AI_PROCESSING.name(), TicketStatus.MANUAL_REVIEW.name())
                .isNotNull("sla_deadline")
                .and(query -> query.eq("sla_warning", 0).or().eq("sla_escalated", 0)));
        int changed = 0;
        for (Ticket ticket : tickets) {
            changed += processOne(ticket, now) ? 1 : 0;
        }
        return changed;
    }

    private boolean processOne(Ticket ticket, LocalDateTime now) {
        boolean changed = false;
        String status = ticket.getStatus();
        if (Integer.valueOf(0).equals(ticket.getSlaWarning()) && reachedWarningPoint(ticket, now)) {
            ticket.setSlaWarning(1);
            changed = true;
            operationLogService.record(ticket.getId(), "SLA_WARNING", null, "SYSTEM",
                    status, status, "SLA剩余时间不足总时长的25%");
        }
        if (Integer.valueOf(0).equals(ticket.getSlaEscalated())
                && !now.isBefore(ticket.getSlaDeadline())) {
            TicketPriority current = TicketPriority.from(ticket.getPriority());
            TicketPriority upgraded = current.next();
            ticket.setSlaWarning(1);
            ticket.setSlaEscalated(1);
            ticket.setPriority(upgraded.name());
            changed = true;
            operationLogService.record(ticket.getId(), "SLA_ESCALATE", null, "SYSTEM",
                    status, status, "SLA已超时，优先级由" + current.name() + "升级为" + upgraded.name());
        }
        if (changed) {
            ticket.setUpdateTime(now);
            ticketMapper.updateById(ticket);
        }
        return changed;
    }

    private boolean reachedWarningPoint(Ticket ticket, LocalDateTime now) {
        if (ticket.getCreateTime() == null || ticket.getSlaDeadline() == null) {
            return false;
        }
        Duration total = Duration.between(ticket.getCreateTime(), ticket.getSlaDeadline());
        if (total.isNegative() || total.isZero()) {
            return true;
        }
        LocalDateTime warningAt = ticket.getSlaDeadline().minus(total.dividedBy(4));
        return !now.isBefore(warningAt);
    }
}
