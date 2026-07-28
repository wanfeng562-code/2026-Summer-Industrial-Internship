package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketMessage;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.mapper.TicketMessageMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketAiContextServiceTest {

    private final TicketMapper ticketMapper = mock(TicketMapper.class);
    private final OrdersMapper ordersMapper = mock(OrdersMapper.class);
    private final TicketMessageMapper messageMapper = mock(TicketMessageMapper.class);
    private final TicketAiContextService service =
            new TicketAiContextService(ticketMapper, ordersMapper, messageMapper);

    @Test
    void buildsContextOnlyFromOwnedTicketAndOrder() {
        Ticket ticket = ticket(1L, 10L, 20L);
        Orders order = order(20L, 10L);
        TicketMessage message = new TicketMessage();
        message.setSenderType("USER");
        message.setContent("物流还没有更新");
        when(ticketMapper.selectById(1L)).thenReturn(ticket);
        when(ordersMapper.selectById(20L)).thenReturn(order);
        when(messageMapper.selectTicketMessageByTicketId(1L)).thenReturn(List.of(message));

        assertThat(service.buildOwnedContext(1L, 10L))
                .contains("测试工单", "ORDER-20", "物流还没有更新");
    }

    @Test
    void rejectsTicketOwnedByAnotherUser() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 11L, 20L));

        assertForbidden(() -> service.buildOwnedContext(1L, 10L));
    }

    @Test
    void rejectsOrderOwnedByAnotherUser() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 10L, 20L));
        when(ordersMapper.selectById(20L)).thenReturn(order(20L, 11L));

        assertForbidden(() -> service.buildOwnedContext(1L, 10L));
    }

    private Ticket ticket(Long id, Long userId, Long orderId) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setUserId(userId);
        ticket.setOrderId(orderId);
        ticket.setTitle("测试工单");
        ticket.setCategory("LOGISTICS");
        ticket.setStatus("AI_PROCESSING");
        return ticket;
    }

    private Orders order(Long id, Long userId) {
        Orders order = new Orders();
        order.setId(id);
        order.setUserId(userId);
        order.setOrderNo("ORDER-20");
        order.setProductName("测试商品");
        order.setOrderStatus("SHIPPED");
        order.setLogisticsStatus("SHIPPED");
        return order;
    }

    private void assertForbidden(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }
}
