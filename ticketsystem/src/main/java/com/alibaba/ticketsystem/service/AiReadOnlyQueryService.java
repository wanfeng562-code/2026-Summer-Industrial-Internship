package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.AfterSalePolicy;
import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiReadOnlyQueryService {

    private final OrdersMapper ordersMapper;
    private final TicketMapper ticketMapper;
    private final UserService userService;
    private final AfterSalePolicyService policyService;
    private final FaqService faqService;

    public String queryOrder(String orderNo) {
        SysUser currentUser = userService.requireCurrentUser();
        Orders order = ordersMapper.getOrdersByOrderNo(orderNo);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            return "未找到该订单";
        }
        if ("USER".equals(currentUser.getRole()) && !currentUser.getId().equals(order.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权查询该订单");
        }
        if (!"USER".equals(currentUser.getRole()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前角色无权查询订单");
        }
        return "订单号=" + order.getOrderNo()
                + "，商品=" + order.getProductName()
                + "，总金额=" + order.getTotalAmount()
                + "，订单状态=" + order.getOrderStatus()
                + "，支付状态=" + order.getPaymentStatus()
                + "，物流状态=" + order.getLogisticsStatus()
                + "，物流单号=" + Optional.ofNullable(order.getLogisticsNo()).orElse("暂无");
    }

    public String queryTicket(Long ticketId) {
        SysUser currentUser = userService.requireCurrentUser();
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())) {
            return "未找到该工单";
        }
        boolean allowed = "ADMIN".equals(currentUser.getRole())
                || ("USER".equals(currentUser.getRole())
                    && currentUser.getId().equals(ticket.getUserId()))
                || ("AGENT".equals(currentUser.getRole())
                    && (currentUser.getId().equals(ticket.getAgentId())
                        || (ticket.getAgentId() == null && "MANUAL_REVIEW".equals(ticket.getStatus()))));
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权查询该工单");
        }
        return "工单号=" + ticket.getTicketNo()
                + "，标题=" + ticket.getTitle()
                + "，分类=" + ticket.getCategory()
                + "，状态=" + ticket.getStatus()
                + "，优先级=" + ticket.getPriority()
                + "，SLA截止=" + ticket.getSlaDeadline();
    }

    public String queryCurrentUser() {
        SysUser currentUser = userService.requireCurrentUser();
        return "当前用户昵称=" + currentUser.getNickname()
                + "，角色=" + currentUser.getRole()
                + "，信誉分=" + currentUser.getReputationScore();
    }

    public String queryPolicy(String category, BigDecimal amount) {
        SysUser currentUser = userService.requireCurrentUser();
        Optional<AfterSalePolicy> matched =
                policyService.match(category, amount, currentUser.getReputationScore());
        if (matched.isEmpty()) {
            return "没有匹配的已启用售后策略，建议转人工确认";
        }
        AfterSalePolicy policy = matched.get();
        return "匹配策略=" + policy.getPolicyName()
                + "，建议动作=" + policy.getAction()
                + "，SLA小时=" + Optional.ofNullable(policy.getSlaHours()).orElse(0)
                + "。该结果仅供建议，任何业务写入仍需后端专用接口校验。";
    }

    public String queryFaq(String keyword, String category) {
        return faqService.search(keyword, category).stream()
                .limit(5)
                .map(faq -> "问：" + faq.getQuestion() + "\n答：" + faq.getAnswer())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("未找到匹配的FAQ，建议转人工确认");
    }
}
