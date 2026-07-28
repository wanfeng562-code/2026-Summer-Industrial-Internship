package com.alibaba.ticketsystem.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketWorkflowServiceTest {

    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private TicketMessageMapper messageMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private UserService userService;
    @Mock
    private TicketOperationLogService logService;

    private TicketWorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new TicketWorkflowService(
                ticketMapper, messageMapper, userMapper, userService, logService);
    }

    @Test
    void unassignedManualTicketCanBeClaimedAtomically() {
        Ticket ticket = ticket(1L, 4L, null, "MANUAL_REVIEW");
        SysUser agent = user(2L, "AGENT");
        when(ticketMapper.selectById(1L)).thenReturn(ticket);
        when(userService.requireCurrentUser()).thenReturn(agent);
        when(ticketMapper.claim(1L, 2L)).thenReturn(1);

        workflowService.claim(1L);

        verify(ticketMapper).claim(1L, 2L);
        verify(logService).record(1L, "CLAIM", 2L, "AGENT",
                "MANUAL_REVIEW", "MANUAL_REVIEW", "客服接单");
    }

    @Test
    void concurrentClaimConflictReturns409() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 4L, null, "MANUAL_REVIEW"));
        when(userService.requireCurrentUser()).thenReturn(user(2L, "AGENT"));
        when(ticketMapper.claim(1L, 2L)).thenReturn(0);

        assertStatus(() -> workflowService.claim(1L), HttpStatus.CONFLICT);
        verifyNoInteractions(logService);
    }

    @Test
    void anotherAgentCannotResolveTicket() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 4L, 3L, "MANUAL_REVIEW"));
        when(userService.requireCurrentUser()).thenReturn(user(2L, "AGENT"));

        TicketResolveRequest request = new TicketResolveRequest();
        request.setContent("已经处理完成");

        assertStatus(() -> workflowService.resolve(1L, request), HttpStatus.FORBIDDEN);
        verify(ticketMapper, never()).transitionStatus(anyLong(), anyString(), anyString(), any(), any());
    }

    @Test
    void assignedAgentCanResolveWithTraceableMessage() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 4L, 2L, "MANUAL_REVIEW"));
        when(userService.requireCurrentUser()).thenReturn(user(2L, "AGENT"));
        when(ticketMapper.transitionStatus(eq(1L), eq("MANUAL_REVIEW"), eq("RESOLVED"),
                any(LocalDateTime.class), isNull())).thenReturn(1);
        TicketResolveRequest request = new TicketResolveRequest();
        request.setContent("已为用户完成换货");

        workflowService.resolve(1L, request);

        ArgumentCaptor<TicketMessage> messageCaptor = ArgumentCaptor.forClass(TicketMessage.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSenderType()).isEqualTo("AGENT");
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("已为用户完成换货");
        verify(logService).record(1L, "RESOLVE", 2L, "AGENT",
                "MANUAL_REVIEW", "RESOLVED", "已为用户完成换货");
    }

    @Test
    void unresolvedTicketCannotBeClosed() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 4L, 2L, "MANUAL_REVIEW"));
        when(userService.requireCurrentUser()).thenReturn(user(2L, "AGENT"));
        TicketCloseRequest request = new TicketCloseRequest();
        request.setReason("用户确认");

        assertStatus(() -> workflowService.close(1L, request), HttpStatus.CONFLICT);
    }

    @Test
    void ownerCanTransferAiTicketToManualReview() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 4L, null, "AI_PROCESSING"));
        when(userService.requireCurrentUser()).thenReturn(user(4L, "USER"));
        when(ticketMapper.transitionStatus(1L, "AI_PROCESSING", "MANUAL_REVIEW", null, null))
                .thenReturn(1);

        workflowService.transferToManual(1L);

        verify(messageMapper).insert(any(TicketMessage.class));
        verify(logService).record(1L, "TRANSFER_MANUAL", 4L, "USER",
                "AI_PROCESSING", "MANUAL_REVIEW", "用户主动请求转人工");
    }

    @Test
    void adminCanAssignAvailableAgent() {
        Ticket current = ticket(1L, 4L, null, "MANUAL_REVIEW");
        when(ticketMapper.selectById(1L)).thenReturn(current);
        when(userService.requireCurrentUser()).thenReturn(user(1L, "ADMIN"));
        SysUser target = user(2L, "AGENT");
        target.setNickname("张客服");
        when(userMapper.selectById(2L)).thenReturn(target);
        when(ticketMapper.assignAgent(1L, 2L)).thenReturn(1);
        TicketAssignRequest request = new TicketAssignRequest();
        request.setAgentId(2L);

        workflowService.assign(1L, request);

        verify(logService).record(1L, "ASSIGN", 1L, "ADMIN",
                "MANUAL_REVIEW", "MANUAL_REVIEW", "分配客服：张客服（ID=2）");
    }

    private void assertStatus(Runnable action, HttpStatus status) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(status));
    }

    private Ticket ticket(Long id, Long userId, Long agentId, String status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setUserId(userId);
        ticket.setAgentId(agentId);
        ticket.setStatus(status);
        ticket.setDeleted(0);
        return ticket;
    }

    private SysUser user(Long id, String role) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        user.setDeleted(0);
        return user;
    }
}
