package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.domain.TicketStatus;
import com.alibaba.ticketsystem.dto.TicketAssignRequest;
import com.alibaba.ticketsystem.dto.TicketCloseRequest;
import com.alibaba.ticketsystem.dto.TicketFollowUpRequest;
import com.alibaba.ticketsystem.dto.TicketRejectRequest;
import com.alibaba.ticketsystem.dto.TicketPriorityRequest;
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
    private final ContentModerationService contentModerationService;

    @Transactional
    public void claim(Long ticketId) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireRole(operator, "AGENT", "只有客服可以接单");
        requireStatus(ticket, TicketStatus.MANUAL_REVIEW);
        if (ticket.getGroupId() != null && !ticket.getGroupId().equals(operator.getAgentGroupId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "该工单已分配给其他坐席组");
        }

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
        if (ticket.getGroupId() != null && !ticket.getGroupId().equals(agent.getAgentGroupId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "目标客服不属于该工单负责坐席组");
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
    public void followUp(Long ticketId, TicketFollowUpRequest request) {
        contentModerationService.validateUserContent(request.getContent());
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireNotArchived(ticket);
        if (TicketStatus.CLOSED.name().equals(ticket.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "已关闭工单不能继续跟进");
        }
        boolean owner = "USER".equals(operator.getRole()) && operator.getId().equals(ticket.getUserId());
        boolean handler = "ADMIN".equals(operator.getRole())
                || ("AGENT".equals(operator.getRole()) && operator.getId().equals(ticket.getAgentId()));
        if (!owner && !handler) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权跟进该工单");
        }
        String before = ticket.getStatus();
        String after = before;
        if (owner && (TicketStatus.RESOLVED.name().equals(before) || TicketStatus.REJECTED.name().equals(before))) {
            if (ticketMapper.transitionStatus(ticketId, before, TicketStatus.MANUAL_REVIEW.name(), null, null) != 1) {
                throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
            }
            after = TicketStatus.MANUAL_REVIEW.name();
        }
        insertMessage(ticketId, operator, operator.getRole(), "FOLLOW_UP", request.getContent().trim());
        operationLogService.record(ticketId, "FOLLOW_UP", operator.getId(), operator.getRole(),
                before, after, request.getContent().trim());
    }

    @Transactional
    public void reject(Long ticketId, TicketRejectRequest request) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireStatus(ticket, TicketStatus.MANUAL_REVIEW);
        requireAssignedAgentOrAdmin(ticket, operator);
        if (ticketMapper.transitionStatus(ticketId, TicketStatus.MANUAL_REVIEW.name(),
                TicketStatus.REJECTED.name(), null, null) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
        }
        insertMessage(ticketId, operator, "SYSTEM", "SYSTEM", "工单已驳回：" + request.getReason().trim());
        operationLogService.record(ticketId, "REJECT", operator.getId(), operator.getRole(),
                TicketStatus.MANUAL_REVIEW.name(), TicketStatus.REJECTED.name(), request.getReason().trim());
    }

    @Transactional
    public void archive(Long ticketId) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireRole(operator, "ADMIN", "仅管理员可以归档工单");
        if (ticketMapper.archive(ticketId) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "仅已关闭或已驳回且未归档的工单可归档");
        }
        operationLogService.record(ticketId, "ARCHIVE", operator.getId(), operator.getRole(),
                ticket.getStatus(), ticket.getStatus(), "管理员归档工单");
    }

    @Transactional
    public void adjustPriority(Long ticketId, TicketPriorityRequest request) {
        Ticket ticket = requireTicket(ticketId);
        SysUser operator = userService.requireCurrentUser();
        requireRole(operator, "ADMIN", "仅管理员可以调整工单优先级");
        requireNotArchived(ticket);
        String priority = request.getPriority().trim();
        if (priority.equals(ticket.getPriority())) {
            throw new ApiException(HttpStatus.CONFLICT, "工单已经是该优先级");
        }
        if (ticketMapper.updatePriorityById(ticketId, priority) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "优先级更新失败，请刷新后重试");
        }
        operationLogService.record(ticketId, "PRIORITY_CHANGE", operator.getId(), operator.getRole(),
                ticket.getStatus(), ticket.getStatus(), "优先级：" + ticket.getPriority() + " -> " + priority);
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

    private void requireNotArchived(Ticket ticket) {
        if (Integer.valueOf(1).equals(ticket.getArchived())) {
            throw new ApiException(HttpStatus.CONFLICT, "已归档工单只允许查看历史记录");
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
