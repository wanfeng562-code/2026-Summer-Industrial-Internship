package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketMessage;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.mapper.TicketMessageMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketAiContextService {

    private final TicketMapper ticketMapper;
    private final OrdersMapper ordersMapper;
    private final TicketMessageMapper messageMapper;

    public String buildOwnedContext(Long ticketId, Long expectedUserId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "工单不存在");
        }
        if (!expectedUserId.equals(ticket.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "AI无权读取该工单");
        }
        Orders order = ordersMapper.selectById(ticket.getOrderId());
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())
                || !expectedUserId.equals(order.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "AI无权读取关联订单");
        }

        List<TicketMessage> messages = messageMapper.selectTicketMessageByTicketId(ticketId);
        int fromIndex = Math.max(0, messages.size() - 10);
        StringBuilder history = new StringBuilder();
        for (TicketMessage message : messages.subList(fromIndex, messages.size())) {
            history.append(message.getSenderType())
                    .append("：")
                    .append(limit(message.getContent(), 500))
                    .append('\n');
        }
        return """
                工单标题：%s
                工单分类：%s
                当前状态：%s
                关联订单号：%s
                商品：%s
                订单状态：%s
                物流状态：%s
                最近消息：
                %s
                """.formatted(
                ticket.getTitle(), ticket.getCategory(), ticket.getStatus(),
                order.getOrderNo(), order.getProductName(), order.getOrderStatus(),
                order.getLogisticsStatus(), history);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "…";
    }
}
