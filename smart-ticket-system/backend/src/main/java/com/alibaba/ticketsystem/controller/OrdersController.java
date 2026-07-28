package com.alibaba.ticketsystem.controller;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.service.OrdersService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.OrderVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 控制层
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@RestController    //返回数据对象
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    //实现订单分页列表查询
    @SaCheckPermission("order:query")
    @GetMapping("/orders/{page}/{pageSize}")
    public R<?> pageOrders(@PathVariable("page") Integer page,
                                      @PathVariable("pageSize") Integer pageSize){
        Page<Orders> po = ordersService.pageOrders(page, pageSize);
        return R.success("订单分页查询成功", po);
    }

    //查询订单详情
    @SaCheckPermission("order:query")
    @GetMapping("/orders/detail/{id}")
    public R<?> getOrders(@PathVariable("id") Long id){
        OrderVo orders = ordersService.getOrders(id);
        return R.success("订单详情查询成功", orders);
    }

}
