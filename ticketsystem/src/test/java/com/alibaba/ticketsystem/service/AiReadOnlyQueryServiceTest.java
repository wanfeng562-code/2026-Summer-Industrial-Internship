package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiReadOnlyQueryServiceTest {

    private final OrdersMapper ordersMapper = mock(OrdersMapper.class);
    private final TicketMapper ticketMapper = mock(TicketMapper.class);
    private final UserService userService = mock(UserService.class);
    private final AfterSalePolicyService policyService = mock(AfterSalePolicyService.class);
    private final FaqService faqService = mock(FaqService.class);
    private final AiReadOnlyQueryService service =
            new AiReadOnlyQueryService(ordersMapper, ticketMapper, userService, policyService, faqService);

    @Test
    void userCanReadOwnOrderSummary() {
        when(userService.requireCurrentUser()).thenReturn(user(10L, "USER"));
        when(userService.requireActiveUser(10L)).thenReturn(user(10L, "USER"));
        when(ordersMapper.getOrdersByOrderNo("ORDER-1")).thenReturn(order(10L));

        assertThat(service.queryOrder("ORDER-1"))
                .contains("ORDER-1", "测试商品", "SHIPPED");
    }

    @Test
    void userCannotReadAnotherUsersOrder() {
        when(userService.requireCurrentUser()).thenReturn(user(10L, "USER"));
        when(userService.requireActiveUser(10L)).thenReturn(user(10L, "USER"));
        when(ordersMapper.getOrdersByOrderNo("ORDER-1")).thenReturn(order(11L));

        assertThatThrownBy(() -> service.queryOrder("ORDER-1"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void agentCannotUseOrderQueryAsABypass() {
        when(userService.requireCurrentUser()).thenReturn(user(10L, "AGENT"));
        when(userService.requireActiveUser(10L)).thenReturn(user(10L, "AGENT"));
        when(ordersMapper.getOrdersByOrderNo("ORDER-1")).thenReturn(order(11L));

        assertThatThrownBy(() -> service.queryOrder("ORDER-1"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void userCanListOnlyOwnedOrders() {
        when(userService.requireActiveUser(10L)).thenReturn(user(10L, "USER"));
        when(ordersMapper.selectCount(any())).thenReturn(1L);
        when(ordersMapper.selectList(any())).thenReturn(List.of(order(10L)));

        assertThat(service.listOrders(10L))
                .contains("共 1 条", "ORDER-1", "测试商品");
    }

    @Test
    void ticketNumberCanBeQueriedInsteadOfNumericDatabaseId() {
        when(userService.requireActiveUser(10L)).thenReturn(user(10L, "USER"));
        when(ticketMapper.selectOne(any())).thenReturn(ticket(10L));

        assertThat(service.queryTicket("TKC2390428", 10L))
                .contains("TKC2390428", "测试工单");
    }

    @Test
    void userCanListOnlyAccessibleTickets() {
        when(userService.requireActiveUser(10L)).thenReturn(user(10L, "USER"));
        when(ticketMapper.selectCount(any())).thenReturn(1L);
        when(ticketMapper.selectList(any())).thenReturn(List.of(ticket(10L)));

        assertThat(service.listTickets(10L))
                .contains("共 1 条", "TKC2390428", "测试工单");
    }

    @Test
    void agentCannotQueryUnassignedTicketFromAnotherGroup() {
        SysUser agent = user(2L, "AGENT");
        agent.setAgentGroupId(10L);
        Ticket ticket = ticket(99L);
        ticket.setGroupId(20L);
        ticket.setAgentId(null);
        when(userService.requireActiveUser(2L)).thenReturn(agent);
        when(ticketMapper.selectOne(any())).thenReturn(ticket);

        assertThatThrownBy(() -> service.queryTicket("TKC2390428", 2L))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private SysUser user(Long id, String role) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        user.setNickname("测试用户");
        return user;
    }

    private Orders order(Long userId) {
        Orders order = new Orders();
        order.setOrderNo("ORDER-1");
        order.setUserId(userId);
        order.setProductName("测试商品");
        order.setOrderStatus("SHIPPED");
        order.setPaymentStatus("PAID");
        order.setLogisticsStatus("SHIPPED");
        return order;
    }

    private Ticket ticket(Long userId) {
        Ticket ticket = new Ticket();
        ticket.setId(20L);
        ticket.setTicketNo("TKC2390428");
        ticket.setUserId(userId);
        ticket.setTitle("测试工单");
        ticket.setCategory("LOGISTICS");
        ticket.setStatus("MANUAL_REVIEW");
        ticket.setPriority("MEDIUM");
        ticket.setDeleted(0);
        return ticket;
    }
}
