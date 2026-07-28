package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.service.OrdersService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.OrderVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单表 控制层
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@Validated
@RestController
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @SaCheckPermission("order:query")
    @GetMapping("/orders")
    public R<?> pageOrders(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "current必须大于0") Integer current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size必须大于0")
            @Max(value = 100, message = "size不能超过100") Integer size) {
        Page<Orders> result = ordersService.pageOrders(current, size);
        return R.success("订单分页查询成功", result);
    }

    /**
     * 兼容 7-28 前端旧路径，成员 B 完成 API 升级后可移除。
     */
    @SaCheckPermission("order:query")
    //实现订单分页列表查询
    @GetMapping("/orders/{page}/{pageSize}")
    public R<?> pageOrders(@PathVariable("page") Integer page,
                                      @PathVariable("pageSize") Integer pageSize){
        Page<Orders> po = ordersService.pageOrders(page, pageSize);
        return R.success("订单分页查询成功", po);
    }

    //查询订单详情
    @SaCheckPermission("order:query")
    @GetMapping("/orders/{id}")
    public R<?> getOrders(@PathVariable("id") Long id){
        OrderVo orders = ordersService.getOrders(id);
        return R.success("订单详情查询成功", orders);
    }

}
