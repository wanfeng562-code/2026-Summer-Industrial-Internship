package com.alibaba.ticketsystem;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.service.OrdersService;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private OrdersService ordersService;

    @Test
    void assertOrderOwnedByCurrentUser_rejectsOthersOrder() {
        Orders orders = new Orders();
        orders.setId(1L);
        orders.setUserId(99L);
        Mockito.when(ordersMapper.selectById(1L)).thenReturn(orders);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            ApiException ex = Assertions.assertThrows(ApiException.class,
                    () -> ordersService.assertOrderOwnedByCurrentUser(1L));
            Assertions.assertEquals(403, ex.getCode());
        }
    }

    @Test
    void assertOrderOwnedByCurrentUser_acceptsOwnOrder() {
        Orders orders = new Orders();
        orders.setId(1L);
        orders.setUserId(1L);
        Mockito.when(ordersMapper.selectById(1L)).thenReturn(orders);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            Orders result = ordersService.assertOrderOwnedByCurrentUser(1L);
            Assertions.assertEquals(1L, result.getId());
        }
    }

    @Test
    void assertOrderReadable_userCannotReadOthers() {
        Orders orders = new Orders();
        orders.setId(2L);
        orders.setUserId(2L);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRole("USER");
        Mockito.when(sysUserMapper.selectById(1L)).thenReturn(user);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            ApiException ex = Assertions.assertThrows(ApiException.class,
                    () -> ordersService.assertOrderReadable(orders));
            Assertions.assertEquals(403, ex.getCode());
        }
    }

    @Test
    void assertOrderOwnedByCurrentUser_notFound() {
        Mockito.when(ordersMapper.selectById(999L)).thenReturn(null);
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            ApiException ex = Assertions.assertThrows(ApiException.class,
                    () -> ordersService.assertOrderOwnedByCurrentUser(999L));
            Assertions.assertEquals(404, ex.getCode());
        }
    }
}
