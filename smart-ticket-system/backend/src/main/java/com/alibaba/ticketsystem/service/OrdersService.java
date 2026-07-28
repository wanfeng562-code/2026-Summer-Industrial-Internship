package com.alibaba.ticketsystem.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.vo.OrderVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    public Page<Orders> pageOrders(int currentPage, int pageSize) {
        Long loginId = StpUtil.getLoginIdAsLong();
        SysUser current = sysUserMapper.selectById(loginId);
        if (current == null) {
            throw new ApiException(401, "未登录或登录已失效");
        }

        Page<Orders> page = new Page<>(currentPage, pageSize);
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        // 普通用户仅能查看自己的订单；客服/管理员可查看全量
        if ("USER".equals(current.getRole())) {
            qw.eq("user_id", loginId);
        }
        qw.eq("deleted", 0);
        qw.orderByDesc("id");
        return ordersMapper.selectPage(page, qw);
    }

    public OrderVo getOrders(Long id) {
        Orders orders = ordersMapper.selectById(id);
        if (orders == null) {
            throw new ApiException(404, "该订单不存在");
        }
        assertOrderReadable(orders);

        SysUser sysUser = sysUserMapper.selectById(orders.getUserId());
        if (sysUser == null) {
            throw new ApiException(404, "该订单的用户不存在");
        }
        OrderVo orderVo = new OrderVo();
        orderVo.setId(orders.getId());
        orderVo.setOrderNo(orders.getOrderNo());
        orderVo.setUserId(orders.getUserId());
        orderVo.setUsername(sysUser.getUsername());
        orderVo.setProductName(orders.getProductName());
        orderVo.setQuantity(orders.getQuantity());
        orderVo.setUnitPrice(orders.getUnitPrice());
        orderVo.setTotalAmount(orders.getTotalAmount());
        orderVo.setOrderStatus(orders.getOrderStatus());
        orderVo.setPaymentStatus(orders.getPaymentStatus());
        orderVo.setLogisticsStatus(orders.getLogisticsStatus());
        orderVo.setLogisticsNo(orders.getLogisticsNo());
        orderVo.setOrderTime(orders.getOrderTime());
        orderVo.setPayTime(orders.getPayTime());
        orderVo.setDeliverTime(orders.getDeliverTime());
        orderVo.setReceiveTime(orders.getReceiveTime());
        return orderVo;
    }

    /** 校验当前登录用户是否有权读取该订单 */
    public void assertOrderReadable(Orders orders) {
        Long loginId = StpUtil.getLoginIdAsLong();
        SysUser current = sysUserMapper.selectById(loginId);
        if (current == null) {
            throw new ApiException(401, "未登录或登录已失效");
        }
        if ("USER".equals(current.getRole()) && !loginId.equals(orders.getUserId())) {
            throw new ApiException(403, "无权查看他人订单");
        }
    }

    /** 校验订单存在且属于当前用户（创建工单用） */
    public Orders assertOrderOwnedByCurrentUser(Long orderId) {
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new ApiException(404, "该订单不存在");
        }
        Long loginId = StpUtil.getLoginIdAsLong();
        if (!loginId.equals(orders.getUserId())) {
            throw new ApiException(403, "只能为自己的订单创建工单");
        }
        return orders;
    }
}
