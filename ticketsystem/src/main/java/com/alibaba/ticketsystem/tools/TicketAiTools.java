package com.alibaba.ticketsystem.tools;

import com.alibaba.ticketsystem.service.AiReadOnlyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TicketAiTools {

    private final AiReadOnlyQueryService queryService;

    @Tool(description = "按订单号查询当前登录用户有权访问的订单摘要。只读。")
    public String queryAccessibleOrder(
            @ToolParam(description = "订单号，例如 ORD20240001") String orderNo,
            ToolContext toolContext) {
        return queryService.queryOrder(orderNo, currentUserId(toolContext));
    }

    @Tool(description = "按工单号或数字工单ID查询当前登录用户有权访问的工单摘要。只读。工单号示例：TKC2390428。")
    public String queryAccessibleTicket(
            @ToolParam(description = "工单号或数字工单ID") String ticketNoOrId,
            ToolContext toolContext) {
        return queryService.queryTicket(ticketNoOrId, currentUserId(toolContext));
    }

    @Tool(description = "列出当前登录用户有权访问的全部订单摘要。用户询问我的订单、所有订单、订单列表时使用。只读。")
    public String listAccessibleOrders(ToolContext toolContext) {
        return queryService.listOrders(currentUserId(toolContext));
    }

    @Tool(description = "列出当前登录用户有权访问的全部工单摘要。用户询问我的工单、所有工单、工单列表时使用。只读。")
    public String listAccessibleTickets(ToolContext toolContext) {
        return queryService.listTickets(currentUserId(toolContext));
    }

    @Tool(description = "查询当前登录用户的昵称、角色和信誉分。不能查询其他用户。只读。")
    public String queryCurrentUser(ToolContext toolContext) {
        return queryService.queryCurrentUser(currentUserId(toolContext));
    }

    @Tool(description = "按分类、金额和当前用户信誉分匹配已启用策略，只返回建议，不执行任何业务写入。")
    public String queryPolicySuggestion(
            @ToolParam(description = "分类：REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER") String category,
            @ToolParam(description = "相关订单或诉求金额") BigDecimal amount,
            ToolContext toolContext) {
        return queryService.queryPolicy(category, amount, currentUserId(toolContext));
    }

    @Tool(description = "按关键词或分类检索已启用的FAQ知识。只读，最多返回五条摘要。")
    public String searchFaq(
            @ToolParam(description = "检索关键词，可以为空但不能与分类同时为空") String keyword,
            @ToolParam(description = "可选分类：REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER") String category) {
        return queryService.queryFaq(keyword, category);
    }

    private Long currentUserId(ToolContext toolContext) {
        Object value = toolContext.getContext().get("currentUserId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalStateException("AI 工具缺少可信登录身份");
    }
}
