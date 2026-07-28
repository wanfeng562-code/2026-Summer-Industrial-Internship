package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.MessageRequest;
import com.alibaba.ticketsystem.dto.TicketCreateRequest;
import com.alibaba.ticketsystem.dto.TicketUpdateRequest;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.service.TicketService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.TicketVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 工单表 前端控制器
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@RestController  //返回数据对象  控制层  此类实例放入spring容器中@Component
public class TicketController {

    @Autowired
    private TicketService ticketService;

    /*
    *工单分页列表查询
    * http://localhost:8080/tickets?current=1&size=10
     */
    @SaCheckPermission("ticket:query") //判断该接口是否有查询权限
    @GetMapping("/tickets")
    public R<?> pageTickets(@RequestParam(value = "current", defaultValue = "1", required = false) Integer current,
                            @RequestParam(value = "size",defaultValue = "10", required = false) Integer size){
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

    /*
     *更新工单状态
     * 请求体：{ "status": "新状态", "category": "新分类", "priority": "优先级", "agentId": "客服ID" }
     * 使用场景：客服修改工单状态（如从"人工复核"改为"已解决"），或系统自动升级工单优先级
     */
    @SaCheckPermission("ticket:update")
    @PutMapping("/tickets/{ticketId}")
    public R<?> updateTicketStatus(@PathVariable("ticketId") Long ticketId,
                                   @RequestBody TicketUpdateRequest ticketUpdateRequest){
        ticketService.updateTicketStatus(ticketId,ticketUpdateRequest);
        return R.success("工单状态更新成功");
    }



}
