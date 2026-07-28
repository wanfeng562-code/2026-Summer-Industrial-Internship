package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(ordersMapper.getOrdersByOrderNo("ORDER-1")).thenReturn(order(10L));

        assertThat(service.queryOrder("ORDER-1"))
                .contains("ORDER-1", "测试商品", "SHIPPED");
    }

    @Test
    void userCannotReadAnotherUsersOrder() {
        when(userService.requireCurrentUser()).thenReturn(user(10L, "USER"));
        when(ordersMapper.getOrdersByOrderNo("ORDER-1")).thenReturn(order(11L));

        assertThatThrownBy(() -> service.queryOrder("ORDER-1"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void agentCannotUseOrderQueryAsABypass() {
        when(userService.requireCurrentUser()).thenReturn(user(10L, "AGENT"));
        when(ordersMapper.getOrdersByOrderNo("ORDER-1")).thenReturn(order(11L));

        assertThatThrownBy(() -> service.queryOrder("ORDER-1"))
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
}
