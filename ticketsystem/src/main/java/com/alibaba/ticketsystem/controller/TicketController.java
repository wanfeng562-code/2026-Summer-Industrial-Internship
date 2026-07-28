package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.MessageRequest;
import com.alibaba.ticketsystem.dto.TicketAssignRequest;
import com.alibaba.ticketsystem.dto.TicketCloseRequest;
import com.alibaba.ticketsystem.dto.TicketCreateRequest;
import com.alibaba.ticketsystem.dto.TicketResolveRequest;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.service.TicketOperationLogService;
import com.alibaba.ticketsystem.service.TicketService;
import com.alibaba.ticketsystem.service.TicketWorkflowService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.TicketVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 工单表 前端控制器
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@Validated
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketWorkflowService workflowService;
    private final TicketOperationLogService operationLogService;

    /*
    *工单分页列表查询
    * http://localhost:8080/tickets?current=1&size=10
     */
    @SaCheckPermission("ticket:query") //判断该接口是否有查询权限
    @GetMapping("/tickets")
    public R<?> pageTickets(
            @RequestParam(value = "current", defaultValue = "1") @Min(1) Integer current,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(100) Integer size) {
        Page<TicketVo> pt = ticketService.pageTickets(current,size);
        return R.success("工单分页列表查询成功", pt);
    }

    //工单详情
    @SaCheckPermission("ticket:query")
    @GetMapping("/tickets/{ticketId}")
    public R<?> getTicket(@PathVariable("ticketId") Long ticketId){
        TicketVo ticketVo = ticketService.getTicketDetail(ticketId);
        return R.success("工单详情查询成功", ticketVo);
    }

    //创建工单  提交Form表单
    @SaCheckPermission("ticket:add")
    @PostMapping("/tickets")
    public R<?> createTicket(@Valid @RequestBody TicketCreateRequest ticketCreateRequest){
        TicketVo ticketVo = ticketService.createTicket(ticketCreateRequest);
        return R.success("工单创建成功", ticketVo);
    }

    //添加工单消息/沟通消息  @RequestBody 提交JSON对象
    @SaCheckPermission("ticket:message")
    @PostMapping("/tickets/{ticketId}/messages")
    public R<?> addTicketMessage(@PathVariable("ticketId") Long ticketId,
                                 @Valid @RequestBody MessageRequest messageRequest){
        ticketService.addTicketMessage(ticketId,messageRequest);
        return R.success("工单消息/沟通消息添加成功");
    }

    @SaCheckPermission("ticket:query")
    @GetMapping("/tickets/{ticketId}/messages")
    public R<?> listMessages(@PathVariable Long ticketId) {
        return R.success("工单消息查询成功", ticketService.getAccessibleMessages(ticketId));
    }

    @SaCheckPermission("ticket:query")
    @GetMapping("/tickets/{ticketId}/logs")
    public R<?> listOperationLogs(@PathVariable Long ticketId) {
        ticketService.requireViewableTicket(ticketId);
        return R.success("工单操作日志查询成功", operationLogService.list(ticketId));
    }

    @SaCheckPermission("ticket:claim")
    @PostMapping("/tickets/{ticketId}/claim")
    public R<?> claim(@PathVariable Long ticketId) {
        workflowService.claim(ticketId);
        return R.success("接单成功");
    }

    @SaCheckPermission("ticket:assign")
    @PutMapping("/tickets/{ticketId}/assignee")
    public R<?> assign(@PathVariable Long ticketId, @Valid @RequestBody TicketAssignRequest request) {
        workflowService.assign(ticketId, request);
        return R.success("工单分配成功");
    }

    @SaCheckPermission("ticket:resolve")
    @PostMapping("/tickets/{ticketId}/resolve")
    public R<?> resolve(@PathVariable Long ticketId, @Valid @RequestBody TicketResolveRequest request) {
        workflowService.resolve(ticketId, request);
        return R.success("工单已标记为解决");
    }

    @SaCheckPermission("ticket:close")
    @PostMapping("/tickets/{ticketId}/close")
    public R<?> close(@PathVariable Long ticketId, @Valid @RequestBody TicketCloseRequest request) {
        workflowService.close(ticketId, request);
        return R.success("工单已关闭");
    }

    @SaCheckPermission("ticket:message")
    @PostMapping("/tickets/{ticketId}/transfer-manual")
    public R<?> transferToManual(@PathVariable Long ticketId) {
        workflowService.transferToManual(ticketId);
        return R.success("工单已转人工处理");
    }
}
