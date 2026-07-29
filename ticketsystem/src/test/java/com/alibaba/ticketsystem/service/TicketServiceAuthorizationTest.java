package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.MessageRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketMessage;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.mapper.TicketMessageMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceAuthorizationTest {

    @Mock private TicketMapper ticketMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private OrdersMapper ordersMapper;
    @Mock private TicketMessageMapper messageMapper;
    @Mock private TicketMessageService messageService;
    @Mock private AIService aiService;
    @Mock private OrdersService ordersService;
    @Mock private UserService userService;
    @Mock private TicketOperationLogService operationLogService;
    @Mock private AfterSalePolicyService policyService;
    @Mock private AiProcessLogService aiProcessLogService;
    @Mock private TicketCategoryService categoryService;
    @Mock private AgentGroupService groupService;
    @Mock private ContentModerationService contentModerationService;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(
                ticketMapper,
                userMapper,
                ordersMapper,
                messageMapper,
                messageService,
                aiService,
                ordersService,
                userService,
                operationLogService,
                policyService,
                aiProcessLogService,
                categoryService,
                groupService,
                contentModerationService);
    }

    @Test
    void adminMustUseAuditedActionsInsteadOfOrdinaryTicketMessage() {
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setUserId(4L);
        ticket.setAgentId(2L);
        ticket.setStatus("MANUAL_REVIEW");
        ticket.setDeleted(0);
        when(ticketMapper.selectById(10L)).thenReturn(ticket);

        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setRole("ADMIN");
        admin.setDeleted(0);
        when(userService.requireCurrentUser()).thenReturn(admin);

        MessageRequest request = new MessageRequest();
        request.setContent("管理员普通回复");

        assertThatThrownBy(() -> ticketService.addTicketMessage(10L, request))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verify(messageMapper, never()).insert(org.mockito.ArgumentMatchers.<TicketMessage>any());
    }

    @Test
    void archivedTicketRejectsNewMessages() {
        Ticket ticket = ownedTicket("REJECTED");
        ticket.setArchived(1);
        when(ticketMapper.selectById(10L)).thenReturn(ticket);
        MessageRequest request = new MessageRequest();
        request.setContent("继续补充");

        assertConflict(() -> ticketService.addTicketMessage(10L, request));
        verify(messageMapper, never()).insert(org.mockito.ArgumentMatchers.<TicketMessage>any());
    }

    @Test
    void rejectedTicketRequiresAuditedFollowUpAction() {
        when(ticketMapper.selectById(10L)).thenReturn(ownedTicket("REJECTED"));
        when(userService.requireCurrentUser()).thenReturn(user());
        MessageRequest request = new MessageRequest();
        request.setContent("继续补充");

        assertConflict(() -> ticketService.addTicketMessage(10L, request));
        verify(messageMapper, never()).insert(org.mockito.ArgumentMatchers.<TicketMessage>any());
    }

    @Test
    void unassignedTicketInAnotherGroupIsNotVisibleToAgent() {
        Ticket ticket = ownedTicket("MANUAL_REVIEW");
        ticket.setGroupId(20L);
        when(ticketMapper.selectById(10L)).thenReturn(ticket);
        SysUser agent = new SysUser();
        agent.setId(2L);
        agent.setRole("AGENT");
        agent.setAgentGroupId(10L);
        when(userService.requireCurrentUser()).thenReturn(agent);

        assertThatThrownBy(() -> ticketService.getTicketDetail(10L))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verify(messageService, never()).getTicketMessageList(10L);
    }

    private Ticket ownedTicket(String status) {
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setUserId(4L);
        ticket.setStatus(status);
        ticket.setArchived(0);
        ticket.setDeleted(0);
        return ticket;
    }

    private SysUser user() {
        SysUser user = new SysUser();
        user.setId(4L);
        user.setRole("USER");
        return user;
    }

    private void assertConflict(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }
}
