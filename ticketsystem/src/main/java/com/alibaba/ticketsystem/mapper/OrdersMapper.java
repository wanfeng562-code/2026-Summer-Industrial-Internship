package com.alibaba.ticketsystem.mapper;

import com.alibaba.ticketsystem.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("select * from orders where order_no = #{orderNo}")
    public Orders getOrdersByOrderNo(@Param("orderNo") String orderNo);

}
