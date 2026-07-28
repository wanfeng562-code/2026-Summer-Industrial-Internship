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

/**
 * 业务逻辑层
 */

@Service   //说明是业务逻辑层  把当前类的实例放入spring容器中
public class OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    //实现订单分页列表查询
    public Page<Orders> pageOrders(int currentPage, int pageSize){
        //通过当前页和一页多少条记录，得到分页对象

        //登录用户只能看到他自己的订单信息，不能看到别人的订单信息
        Long userId = StpUtil.getLoginIdAsLong();

        Page<Orders> page = new Page<>(currentPage,pageSize);
        QueryWrapper<Orders> qw = new QueryWrapper<>(); //查询对象
        qw.eq("user_id", userId);
        qw.orderByDesc("id");
        Page<Orders> po = ordersMapper.selectPage(page, qw);
        return po;
    }

    //查询订单详情
    public OrderVo getOrders(Long id){
        Orders orders = ordersMapper.selectById(id);
        if(orders == null){
            throw new ApiException("该订单不存在");
        }

        SysUser sysUser = sysUserMapper.selectById(orders.getUserId());
        if(sysUser == null){
            throw new ApiException("该订单的用户不存在");
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
}
