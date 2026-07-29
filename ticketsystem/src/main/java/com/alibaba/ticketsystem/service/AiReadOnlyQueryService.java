package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.AfterSalePolicy;
import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReadOnlyQueryService {

    private static final int MAX_LIST_RESULTS = 50;

    private final OrdersMapper ordersMapper;
    private final TicketMapper ticketMapper;
    private final UserService userService;
    private final AfterSalePolicyService policyService;
    private final FaqService faqService;

    public String queryOrder(String orderNo) {
        return queryOrder(orderNo, userService.requireCurrentUser().getId());
    }

    public String queryOrder(String orderNo, Long currentUserId) {
        SysUser currentUser = userService.requireActiveUser(currentUserId);
        if (!StringUtils.hasText(orderNo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "订单号不能为空");
        }
        String normalizedOrderNo = orderNo.trim().toUpperCase(Locale.ROOT);
        Orders order = ordersMapper.getOrdersByOrderNo(normalizedOrderNo);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            return "未找到该订单";
        }
        ensureOrderAccess(currentUser, order);
        log.info("[AI-TOOL] order query userId={} orderNo={}", currentUser.getId(), normalizedOrderNo);
        return formatOrder(order);
    }

    public String listOrders(Long currentUserId) {
        SysUser currentUser = userService.requireActiveUser(currentUserId);
        ensureOrderRole(currentUser);

        long total = ordersMapper.selectCount(orderScope(currentUser));
        List<Orders> orders = ordersMapper.selectList(orderScope(currentUser)
                .orderByDesc(Orders::getOrderTime)
                .orderByDesc(Orders::getId)
                .last("LIMIT " + MAX_LIST_RESULTS));
        log.info("[AI-TOOL] order list userId={} total={} returned={}",
                currentUser.getId(), total, orders.size());
        if (orders.isEmpty()) {
            return "当前账号下暂无订单";
        }
        String detail = orders.stream()
                .map(this::formatOrder)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        return listHeader("订单", total, orders.size()) + "\n" + detail;
    }

    public String queryTicket(Long ticketId) {
        return queryTicket(String.valueOf(ticketId), userService.requireCurrentUser().getId());
    }

    public String queryTicket(String ticketNoOrId, Long currentUserId) {
        SysUser currentUser = userService.requireActiveUser(currentUserId);
        if (!StringUtils.hasText(ticketNoOrId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "工单号或工单ID不能为空");
        }
        String identifier = ticketNoOrId.trim();
        Ticket ticket;
        if (identifier.matches("\\d+")) {
            ticket = ticketMapper.selectById(Long.valueOf(identifier));
        } else {
            ticket = ticketMapper.selectOne(new LambdaQueryWrapper<Ticket>()
                    .eq(Ticket::getTicketNo, identifier.toUpperCase(Locale.ROOT))
                    .eq(Ticket::getDeleted, 0)
                    .last("LIMIT 1"));
        }
        if (ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())) {
            return "未找到该工单";
        }
        ensureTicketAccess(currentUser, ticket);
        log.info("[AI-TOOL] ticket query userId={} identifier={}", currentUser.getId(), identifier);
        return formatTicket(ticket);
    }

    public String listTickets(Long currentUserId) {
        SysUser currentUser = userService.requireActiveUser(currentUserId);
        ensureTicketRole(currentUser);

        long total = ticketMapper.selectCount(ticketScope(currentUser));
        List<Ticket> tickets = ticketMapper.selectList(ticketScope(currentUser)
                .orderByDesc(Ticket::getCreateTime)
                .orderByDesc(Ticket::getId)
                .last("LIMIT " + MAX_LIST_RESULTS));
        log.info("[AI-TOOL] ticket list userId={} total={} returned={}",
                currentUser.getId(), total, tickets.size());
        if (tickets.isEmpty()) {
            return "当前账号下暂无工单";
        }
        String detail = tickets.stream()
                .map(this::formatTicket)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        return listHeader("工单", total, tickets.size()) + "\n" + detail;
    }

    public String queryCurrentUser() {
        return queryCurrentUser(userService.requireCurrentUser().getId());
    }

    public String queryCurrentUser(Long currentUserId) {
        SysUser currentUser = userService.requireActiveUser(currentUserId);
        return "当前用户昵称=" + currentUser.getNickname()
                + "，角色=" + currentUser.getRole()
                + "，信誉分=" + currentUser.getReputationScore();
    }

    public String queryPolicy(String category, BigDecimal amount) {
        return queryPolicy(category, amount, userService.requireCurrentUser().getId());
    }

    public String queryPolicy(String category, BigDecimal amount, Long currentUserId) {
        SysUser currentUser = userService.requireActiveUser(currentUserId);
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

    private void ensureOrderRole(SysUser currentUser) {
        if (!"USER".equals(currentUser.getRole()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前角色无权查询订单");
        }
    }

    private void ensureOrderAccess(SysUser currentUser, Orders order) {
        ensureOrderRole(currentUser);
        if ("USER".equals(currentUser.getRole()) && !currentUser.getId().equals(order.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权查询该订单");
        }
    }

    private LambdaQueryWrapper<Orders> orderScope(SysUser currentUser) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getDeleted, 0);
        if ("USER".equals(currentUser.getRole())) {
            wrapper.eq(Orders::getUserId, currentUser.getId());
        }
        return wrapper;
    }

    private void ensureTicketRole(SysUser currentUser) {
        if (!List.of("USER", "AGENT", "ADMIN").contains(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前角色无权查询工单");
        }
    }

    private void ensureTicketAccess(SysUser currentUser, Ticket ticket) {
        ensureTicketRole(currentUser);
        boolean allowed = "ADMIN".equals(currentUser.getRole())
                || ("USER".equals(currentUser.getRole())
                    && currentUser.getId().equals(ticket.getUserId()))
                || ("AGENT".equals(currentUser.getRole())
                    && (currentUser.getId().equals(ticket.getAgentId())
                        || (currentUser.getAgentGroupId() != null
                            && currentUser.getAgentGroupId().equals(ticket.getGroupId())
                            && List.of("AI_PROCESSING", "MANUAL_REVIEW").contains(ticket.getStatus()))
                        || (ticket.getAgentId() == null && ticket.getGroupId() == null
                            && "MANUAL_REVIEW".equals(ticket.getStatus()))));
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权查询该工单");
        }
    }

    private LambdaQueryWrapper<Ticket> ticketScope(SysUser currentUser) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getDeleted, 0);
        if ("USER".equals(currentUser.getRole())) {
            wrapper.eq(Ticket::getUserId, currentUser.getId());
        } else if ("AGENT".equals(currentUser.getRole())) {
            wrapper.and(scope -> {
                scope.eq(Ticket::getAgentId, currentUser.getId());
                if (currentUser.getAgentGroupId() != null) {
                    scope.or(groupScope -> groupScope
                            .eq(Ticket::getGroupId, currentUser.getAgentGroupId())
                            .in(Ticket::getStatus, "AI_PROCESSING", "MANUAL_REVIEW"));
                }
                scope.or(unassignedScope -> unassignedScope
                        .isNull(Ticket::getAgentId)
                        .isNull(Ticket::getGroupId)
                        .eq(Ticket::getStatus, "MANUAL_REVIEW"));
            });
        }
        return wrapper;
    }

    private String formatOrder(Orders order) {
        return "订单号=" + order.getOrderNo()
                + "，商品=" + order.getProductName()
                + "，数量=" + Optional.ofNullable(order.getQuantity()).orElse(0)
                + "，总金额=" + order.getTotalAmount()
                + "，订单状态=" + order.getOrderStatus()
                + "，支付状态=" + order.getPaymentStatus()
                + "，物流状态=" + order.getLogisticsStatus()
                + "，物流单号=" + Optional.ofNullable(order.getLogisticsNo()).orElse("暂无")
                + "，下单时间=" + order.getOrderTime();
    }

    private String formatTicket(Ticket ticket) {
        return "工单号=" + ticket.getTicketNo()
                + "，工单ID=" + ticket.getId()
                + "，标题=" + ticket.getTitle()
                + "，分类=" + ticket.getCategory()
                + "，状态=" + ticket.getStatus()
                + "，优先级=" + ticket.getPriority()
                + "，SLA截止=" + ticket.getSlaDeadline()
                + "，创建时间=" + ticket.getCreateTime();
    }

    private String listHeader(String resourceName, long total, int returned) {
        if (total <= returned) {
            return "当前账号有权访问的" + resourceName + "共 " + total + " 条：";
        }
        return "当前账号有权访问的" + resourceName + "共 " + total
                + " 条，以下显示最近 " + returned + " 条：";
    }
}
