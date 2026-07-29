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
                aiProcessLogService);
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
}
