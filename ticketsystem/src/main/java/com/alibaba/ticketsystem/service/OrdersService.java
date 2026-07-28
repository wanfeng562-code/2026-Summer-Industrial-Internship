package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.vo.OrderVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 业务逻辑层
 */

@Service   //说明是业务逻辑层  把当前类的实例放入spring容器中
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersMapper ordersMapper;
    private final SysUserMapper sysUserMapper;
    private final UserService userService;

    //实现订单分页列表查询
    public Page<Orders> pageOrders(int currentPage, int pageSize){
        //通过当前页和一页多少条记录，得到分页对象

        SysUser currentUser = userService.requireCurrentUser();
        Page<Orders> page = new Page<>(currentPage,pageSize);
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        if ("USER".equals(currentUser.getRole())) {
            qw.eq("user_id", currentUser.getId());
        } else if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前角色无权查询订单列表");
        }
        qw.eq("deleted", 0);
        qw.orderByDesc("id");
        return ordersMapper.selectPage(page, qw);
    }

    //查询订单详情
    public OrderVo getOrders(Long id){
        Orders orders = ordersMapper.selectById(id);
        if(orders == null || Integer.valueOf(1).equals(orders.getDeleted())){
            throw new ApiException(HttpStatus.NOT_FOUND, "该订单不存在");
        }

        SysUser currentUser = userService.requireCurrentUser();
        if ("USER".equals(currentUser.getRole()) && !orders.getUserId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权访问该订单");
        }
        if (!"USER".equals(currentUser.getRole()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前角色无权访问订单详情");
        }

        return toOrderVo(orders);
    }

    /**
     * 创建工单时使用：只有订单所属的普通用户可以关联该订单。
     */
    public Orders requireOwnedOrderForTicket(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "该订单不存在");
        }
        SysUser currentUser = userService.requireCurrentUser();
        if (!"USER".equals(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只有普通用户可以为订单创建工单");
        }
        if (!order.getUserId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "不能为他人的订单创建工单");
        }
        return order;
    }

    private OrderVo toOrderVo(Orders orders) {
        SysUser sysUser = sysUserMapper.selectById(orders.getUserId());
        if(sysUser == null){
            throw new ApiException(HttpStatus.NOT_FOUND, "该订单的用户不存在");
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
