package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.domain.TicketStatus;
import com.alibaba.ticketsystem.dto.TicketSatisfactionRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketSatisfaction;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.mapper.TicketSatisfactionMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSatisfactionService {
    private final TicketMapper ticketMapper;
    private final TicketSatisfactionMapper satisfactionMapper;
    private final UserService userService;

    @Transactional
    public TicketSatisfaction submit(Long ticketId, TicketSatisfactionRequest request) {
        SysUser user = userService.requireCurrentUser();
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "工单不存在");
        }
        if (!"USER".equals(user.getRole()) || !user.getId().equals(ticket.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只有工单所属用户可评价");
        }
        if (!TicketStatus.CLOSED.name().equals(ticket.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "仅已关闭工单可评价");
        }
        TicketSatisfaction satisfaction = satisfactionMapper.selectOne(new QueryWrapper<TicketSatisfaction>()
                .eq("ticket_id", ticketId).eq("user_id", user.getId()));
        if (satisfaction == null) {
            satisfaction = new TicketSatisfaction();
            satisfaction.setTicketId(ticketId);
            satisfaction.setUserId(user.getId());
            satisfaction.setCreateTime(LocalDateTime.now());
        }
        satisfaction.setScore(request.getScore());
        satisfaction.setComment(request.getComment() == null ? null : request.getComment().trim());
        satisfaction.setUpdateTime(LocalDateTime.now());
        if (satisfaction.getId() == null) {
            satisfactionMapper.insert(satisfaction);
        } else {
            satisfactionMapper.updateById(satisfaction);
        }
        return satisfaction;
    }

    public List<TicketSatisfaction> all() {
        return satisfactionMapper.selectList(new QueryWrapper<TicketSatisfaction>().orderByDesc("id"));
    }
}
