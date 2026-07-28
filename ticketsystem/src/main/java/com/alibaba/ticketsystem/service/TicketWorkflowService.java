package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.domain.TicketStatus;
import com.alibaba.ticketsystem.dto.TicketAssignRequest;
import com.alibaba.ticketsystem.dto.TicketCloseRequest;
import com.alibaba.ticketsystem.dto.TicketResolveRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketMessage;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.mapper.TicketMessageMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketWorkflowService {

    private final TicketMapper ticketMapper;
    private final TicketMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final UserService userService;
    private final TicketOperationLogService operationLogService;

    @Transactional
    public void claim(Long ticketId) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireRole(operator, "AGENT", "只有客服可以接单");
        requireStatus(ticket, TicketStatus.MANUAL_REVIEW);

        if (operator.getId().equals(ticket.getAgentId())) {
            throw new ApiException(HttpStatus.CONFLICT, "该工单已由当前客服接取");
        }
        if (ticket.getAgentId() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "该工单已被其他客服接取");
        }
        if (ticketMapper.claim(ticketId, operator.getId()) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
        }
        operationLogService.record(ticketId, "CLAIM", operator.getId(), operator.getRole(),
                ticket.getStatus(), ticket.getStatus(), "客服接单");
    }

    @Transactional
    public void assign(Long ticketId, TicketAssignRequest request) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireRole(operator, "ADMIN", "只有管理员可以分配工单");
        requireStatus(ticket, TicketStatus.MANUAL_REVIEW);

        SysUser agent = userMapper.selectById(request.getAgentId());
        if (agent == null || Integer.valueOf(1).equals(agent.getDeleted()) || !"AGENT".equals(agent.getRole())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "目标客服不存在或不可用");
        }
        if (request.getAgentId().equals(ticket.getAgentId())) {
            throw new ApiException(HttpStatus.CONFLICT, "工单已经分配给该客服");
        }
        if (ticketMapper.assignAgent(ticketId, request.getAgentId()) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
        }
        String action = ticket.getAgentId() == null ? "ASSIGN" : "REASSIGN";
        operationLogService.record(ticketId, action, operator.getId(), operator.getRole(),
                ticket.getStatus(), ticket.getStatus(),
                "分配客服：" + agent.getNickname() + "（ID=" + agent.getId() + "）");
    }

    @Transactional
    public void resolve(Long ticketId, TicketResolveRequest request) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireStatus(ticket, TicketStatus.MANUAL_REVIEW);
        requireAssignedAgentOrAdmin(ticket, operator);

        if (ticketMapper.transitionStatus(ticketId, TicketStatus.MANUAL_REVIEW.name(),
                TicketStatus.RESOLVED.name(), LocalDateTime.now(), null) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
        }
        insertMessage(ticketId, operator,
                "ADMIN".equals(operator.getRole()) ? "SYSTEM" : "AGENT",
                "TEXT", request.getContent().trim());
        operationLogService.record(ticketId, "RESOLVE", operator.getId(), operator.getRole(),
                TicketStatus.MANUAL_REVIEW.name(), TicketStatus.RESOLVED.name(), request.getContent().trim());
    }

    @Transactional
    public void close(Long ticketId, TicketCloseRequest request) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireStatus(ticket, TicketStatus.RESOLVED);
        requireAssignedAgentOrAdmin(ticket, operator);

        if (ticketMapper.transitionStatus(ticketId, TicketStatus.RESOLVED.name(),
                TicketStatus.CLOSED.name(), null, LocalDateTime.now()) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
        }
        insertMessage(ticketId, operator, "SYSTEM", "SYSTEM",
                "工单已关闭：" + request.getReason().trim());
        operationLogService.record(ticketId, "CLOSE", operator.getId(), operator.getRole(),
                TicketStatus.RESOLVED.name(), TicketStatus.CLOSED.name(), request.getReason().trim());
    }

    @Transactional
    public void transferToManual(Long ticketId) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        if (!"USER".equals(operator.getRole()) || !operator.getId().equals(ticket.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只有工单所属用户可以请求转人工");
        }
        requireStatus(ticket, TicketStatus.AI_PROCESSING);
        if (ticketMapper.transitionStatus(ticketId, TicketStatus.AI_PROCESSING.name(),
                TicketStatus.MANUAL_REVIEW.name(), null, null) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
        }
        insertMessage(ticketId, operator, "SYSTEM", "SYSTEM", "用户请求转人工客服");
        operationLogService.record(ticketId, "TRANSFER_MANUAL", operator.getId(), operator.getRole(),
                TicketStatus.AI_PROCESSING.name(), TicketStatus.MANUAL_REVIEW.name(), "用户主动请求转人工");
    }

    public Ticket requireTicket(Long ticketId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "该工单不存在");
        }
        return ticket;
    }

    private void requireStatus(Ticket ticket, TicketStatus expected) {
        if (!expected.name().equals(ticket.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "当前工单状态为" + ticket.getStatus() + "，不能执行该操作");
        }
    }

    private void requireRole(SysUser user, String role, String message) {
        if (!role.equals(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void requireAssignedAgentOrAdmin(Ticket ticket, SysUser operator) {
        if ("ADMIN".equals(operator.getRole())) {
            return;
        }
        if (!"AGENT".equals(operator.getRole()) || !operator.getId().equals(ticket.getAgentId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只有已接单客服或管理员可以执行该操作");
        }
    }

    private void insertMessage(Long ticketId,
                               SysUser operator,
                               String senderType,
                               String messageType,
                               String content) {
        TicketMessage message = new TicketMessage();
        message.setTicketId(ticketId);
        message.setUserId(operator.getId());
        message.setSenderType(senderType);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setDeleted(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }
}
