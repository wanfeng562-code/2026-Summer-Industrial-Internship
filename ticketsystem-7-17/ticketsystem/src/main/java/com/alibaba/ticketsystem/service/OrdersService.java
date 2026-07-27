package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
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

    //实现订单分页列表查询
    public Page<Orders> pageOrders(int currentPage, int pageSize){
        //通过当前页和一页多少条记录，得到分页对象
        Page<Orders> page = new Page<>(currentPage,pageSize);
        QueryWrapper<Orders> qw = new QueryWrapper<>(); //查询对象
        qw.orderByDesc("id");
        Page<Orders> po = ordersMapper.selectPage(page, qw);
        return po;
    }
}
