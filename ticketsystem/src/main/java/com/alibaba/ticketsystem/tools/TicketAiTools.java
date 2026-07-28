package com.alibaba.ticketsystem.tools;

import com.alibaba.ticketsystem.service.AiReadOnlyQueryService;
import lombok.RequiredArgsConstructor;
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
            @ToolParam(description = "订单号，例如 ORD20240001") String orderNo) {
        return queryService.queryOrder(orderNo);
    }

    @Tool(description = "按工单ID查询当前登录用户有权访问的工单摘要。只读。")
    public String queryAccessibleTicket(
            @ToolParam(description = "工单ID") Long ticketId) {
        return queryService.queryTicket(ticketId);
    }

    @Tool(description = "查询当前登录用户的昵称、角色和信誉分。不能查询其他用户。只读。")
    public String queryCurrentUser() {
        return queryService.queryCurrentUser();
    }

    @Tool(description = "按分类、金额和当前用户信誉分匹配已启用策略，只返回建议，不执行任何业务写入。")
    public String queryPolicySuggestion(
            @ToolParam(description = "分类：REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER") String category,
            @ToolParam(description = "相关订单或诉求金额") BigDecimal amount) {
        return queryService.queryPolicy(category, amount);
    }

    @Tool(description = "按关键词或分类检索已启用的FAQ知识。只读，最多返回五条摘要。")
    public String searchFaq(
            @ToolParam(description = "检索关键词，可以为空但不能与分类同时为空") String keyword,
            @ToolParam(description = "可选分类：REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER") String category) {
        return queryService.queryFaq(keyword, category);
    }
}
