package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {

    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private UserService userService;

    private OrdersService ordersService;

    @BeforeEach
    void setUp() {
        ordersService = new OrdersService(ordersMapper, userMapper, userService);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void userOrderPageIncludesOwnershipFilter() {
        when(userService.requireCurrentUser()).thenReturn(user(4L, "USER"));
        when(ordersMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ordersService.pageOrders(1, 10);

        ArgumentCaptor<QueryWrapper<Orders>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(ordersMapper).selectPage(any(Page.class), captor.capture());
        assertThat(captor.getValue().getSqlSegment())
                .contains("user_id")
                .contains("deleted");
    }

    @Test
    void agentCannotBrowseOrders() {
        when(userService.requireCurrentUser()).thenReturn(user(2L, "AGENT"));

        assertThatThrownBy(() -> ordersService.pageOrders(1, 10))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(ordersMapper);
    }

    @Test
    void userCannotOpenAnotherUsersOrder() {
        Orders order = order(2L, 5L);
        when(ordersMapper.selectById(2L)).thenReturn(order);
        when(userService.requireCurrentUser()).thenReturn(user(4L, "USER"));

        assertThatThrownBy(() -> ordersService.getOrders(2L))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void ownedOrderCanBeUsedToCreateTicket() {
        Orders order = order(1L, 4L);
        when(ordersMapper.selectById(1L)).thenReturn(order);
        when(userService.requireCurrentUser()).thenReturn(user(4L, "USER"));

        assertThat(ordersService.requireOwnedOrderForTicket(1L)).isSameAs(order);
    }

    @Test
    void anotherUsersOrderCannotBeUsedToCreateTicket() {
        when(ordersMapper.selectById(2L)).thenReturn(order(2L, 5L));
        when(userService.requireCurrentUser()).thenReturn(user(4L, "USER"));

        assertThatThrownBy(() -> ordersService.requireOwnedOrderForTicket(2L))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private SysUser user(Long id, String role) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        user.setDeleted(0);
        return user;
    }

    private Orders order(Long id, Long userId) {
        Orders order = new Orders();
        order.setId(id);
        order.setUserId(userId);
        order.setDeleted(0);
        return order;
    }
}
