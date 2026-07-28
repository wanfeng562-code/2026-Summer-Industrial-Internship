package com.alibaba.ticketsystem.tools;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TicketAiTools {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    //工具1.查询订单详情
    @Tool(description = "根据订单号查询订单详情，返回商品名称、金额、订单状态、物流状态等信息。当用户提到具体订单或商品时调用。")
    public String queryOrder(@ToolParam(description = "订单，例如ORD20240001") String orderNo) {
        Orders orders = ordersMapper.getOrdersByOrderNo(orderNo);
        log.info("[AI-Tool] 查询订单: {}", orderNo);
        if(orders == null) {
            return "未找到订单号为 " + orderNo + " 的订单";
        }
        return String.format(
                "订单号: %s, 商品: %s, 数量: %d, 单价: ¥%s, 总金额: ¥%s, " +
                        "订单状态: %s, 支付状态: %s, 物流状态: %s, 物流单号: %s",
                orders.getOrderNo(), orders.getProductName(), orders.getQuantity(),
                orders.getUnitPrice(), orders.getTotalAmount(),
                orders.getOrderStatus(), orders.getPaymentStatus(),
                orders.getLogisticsStatus(), orders.getLogisticsNo());
    }

    //工具2.查询工单详情
    @Tool(description = "根据工单ID查询工单详情，返回工单号、标题、描述、分类、状态、优先级等信息。")
    public String queryTicket(
            @ToolParam(description = "工单ID") Long ticketId) {
        log.info("[AI-Tool] 查询工单: {}", ticketId);
        try {
            Ticket ticket = ticketMapper.selectById(ticketId);
            return String.format(
                    "工单号: %s, 标题: %s, 分类: %s, 状态: %s, 优先级: %s, " +
                            "描述: %s, 创建时间: %s",
                    ticket.getTicketNo(), ticket.getTitle(),
                    ticket.getCategory(), ticket.getStatus(),
                    ticket.getPriority(), ticket.getDescription(),
                    ticket.getCreateTime());
        } catch (Exception e) {
            return "查询工单失败: " + e.getMessage();
        }
    }


    //工具3.查询用户信息
    @Tool(description = "根据用户ID查询用户信息，返回昵称、信誉分、角色等。用于评估用户信誉以决定处理方式。")
    public String queryUser(
            @ToolParam(description = "用户ID") Long userId) {
        log.info("[AI-Tool] 查询用户: {}", userId);
        try {
            SysUser user = sysUserMapper.selectById(userId);
            return String.format(
                    "用户: %s, 昵称: %s, 信誉分: %d, 角色: %s",
                    user.getUsername(), user.getNickname(),
                    user.getReputationScore(), user.getRole());
        } catch (Exception e) {
            return "查询用户失败: " + e.getMessage();
        }
    }

    //工具4. 更新工单状态
    @Tool(description = "更新工单的优先级。可选值: LOW低, MEDIUM中, HIGH高, URGENT紧急。当用户要求加急处理时调用。")
    public String updateTicketPriority(
            @ToolParam(description = "工单ID") Long ticketId,
            @ToolParam(description = "优先级: LOW/MEDIUM/HIGH/URGENT") String priority) {
        log.info("[AI-Tool] 更新工单优先级: ticketId={}, priority={}", ticketId, priority);
        try {
            ticketMapper.updatePriorityById(ticketId, priority);
            return "工单优先级已更新为: " + priority;
        } catch (Exception e) {
            return "更新失败: " + e.getMessage();
        }
    }

    //工具5.自动处理退货退款

    //工具6.自动处理开发票
}
