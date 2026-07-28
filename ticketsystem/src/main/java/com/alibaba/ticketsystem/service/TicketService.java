package com.alibaba.ticketsystem.service;
import java.time.LocalDateTime;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.dto.MessageRequest;
import com.alibaba.ticketsystem.dto.TicketCreateRequest;
import com.alibaba.ticketsystem.dto.TicketUpdateRequest;
import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.TicketMessage;
import com.alibaba.ticketsystem.mapper.OrdersMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketMessageMapper;
import com.alibaba.ticketsystem.vo.TicketMessageVo;
import java.util.ArrayList;

import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.vo.TicketVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service   // 当前类是业务逻辑类   此类实例放入spring容器中
public class TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private TicketMessageMapper ticketMessageMapper;

    @Autowired
    private TicketMessageService ticketMessageService;

    @Autowired
    private AIService aiService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private UserService userService;

    /** 工单状态中文名称映射 */
    private static final Map<String, String> STATUS_NAMES = Map.of(
            "PENDING", "待处理", "AI_PROCESSING", "AI预处理中",
            "MANUAL_REVIEW", "人工复核", "RESOLVED", "已解决", "CLOSED", "已关闭"
    );

    /** 工单分类中文名称映射 */
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "REFUND", "退款退货", "LOGISTICS", "物流异常",
            "DAMAGE", "商品破损", "INVOICE", "发票问题", "OTHER", "其他"
    );

    //工单详情
    public TicketVo getTicketDetail(Long ticketId){
        Ticket ticket = ticketMapper.selectById(ticketId);
        if(ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())){
            throw new ApiException(HttpStatus.NOT_FOUND, "该工单不存在");
        }
        assertCanView(ticket, userService.requireCurrentUser());
        return  convertToTicketVo(ticket);
    }

    private TicketVo convertToTicketVo(Ticket ticket){
        TicketVo ticketVo = new TicketVo();
        ticketVo.setId(ticket.getId());
        ticketVo.setTicketNo(ticket.getTicketNo());
        ticketVo.setUserId(ticket.getUserId());
        ticketVo.setAgentId(ticket.getAgentId());
        ticketVo.setOrderId(ticket.getOrderId());
        ticketVo.setTitle(ticket.getTitle());
        ticketVo.setDescription(ticket.getDescription());
        ticketVo.setCategory(ticket.getCategory());
        ticketVo.setCategoryName(CATEGORY_NAMES.get(ticket.getCategory()));
        ticketVo.setStatus(ticket.getStatus());
        ticketVo.setStatusName(STATUS_NAMES.get(ticket.getStatus()));
        ticketVo.setPriority(ticket.getPriority());
        ticketVo.setSlaWarning(ticket.getSlaWarning());
        ticketVo.setSlaEscalated(ticket.getSlaEscalated());
        ticketVo.setSlaDeadline(ticket.getSlaDeadline());
        ticketVo.setCreateTime(ticket.getCreateTime());
        ticketVo.setUpdateTime(ticket.getUpdateTime());
        ticketVo.setMessages(new ArrayList<TicketMessageVo>());

        SysUser user = sysUserMapper.selectById(ticket.getUserId());
        if(user != null){
            ticketVo.setUsername(user.getUsername());
            ticketVo.setUserNickname(user.getNickname());
        }

        SysUser agent = sysUserMapper.selectById(ticket.getAgentId());
        if(agent != null){
            ticketVo.setAgentName(agent.getNickname());
        }

        Orders orders = ordersMapper.selectById(ticket.getOrderId());
        if(orders != null){
            ticketVo.setOrderNo(orders.getOrderNo());
        }
        List<TicketMessageVo> msgVos = ticketMessageService.getTicketMessageList(ticket.getId());
        ticketVo.setMessages(msgVos);
        return ticketVo;
    }

    /**
     * 工单分页列表查询
     * 如果登录用户角色是USER，则只能看到自己的工单信息
     * 如果登录用户角色是AGENT  ADMIN ，则能看到全部的工单信息
     */
    public Page<TicketVo> pageTickets(int current, int size){
        SysUser user = userService.requireCurrentUser();
        Page<TicketVo> page = new Page<>(current, size);
        return ticketMapper.pageTicketVo(page, user.getId(), user.getRole());
    }

    @Transactional   //这是一个事务
    public TicketVo createTicket(TicketCreateRequest ticketCreateRequest){


        //1.把当前工单对应的订单信息拿到
        Orders orders = ordersService.requireOwnedOrderForTicket(ticketCreateRequest.getOrderId());

        //集成Sa-Token，获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        //2.如果问题分类为空，则AI判断问题的类型
        String category = ticketCreateRequest.getCategory();
        if(category == null || category.isBlank()){
            //调用AI，用description判断问题类型，等接入AI再完成
            category = aiService.classify(ticketCreateRequest.getDescription());
            if("CONSULT".equals(category) || "COMPLAINT".equals(category)){
                category = "OTHER";
            }
        }
        //3.创建工单
        Ticket ticket = new Ticket();
        ticket.setTicketNo("TK" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        ticket.setUserId(userId);
        ticket.setOrderId(orders.getId());
        ticket.setTitle(ticketCreateRequest.getTitle());
        ticket.setDescription(ticketCreateRequest.getDescription());
        ticket.setCategory(category);
        ticket.setStatus("AI_PROCESSING");
        String priority = ticketCreateRequest.getPriority();
        ticket.setPriority(priority == null || priority.isBlank() ? "MEDIUM" : priority);
        ticket.setSlaWarning(0);
        ticket.setSlaEscalated(0);
        ticket.setSlaDeadline(LocalDateTime.now().plusHours(2));  //要求2小时内响应完成
        ticket.setDeleted(0);
        ticket.setCreateTime(LocalDateTime.now());
        ticketMapper.insert(ticket);

        //4.创建工单消息
        TicketMessage ticketMessage = new TicketMessage();
        ticketMessage.setTicketId(ticket.getId());
        ticketMessage.setUserId(userId);
        ticketMessage.setSenderType("USER");
        ticketMessage.setMessageType("TEXT");
        ticketMessage.setContent(ticketCreateRequest.getDescription());
        ticketMessage.setDeleted(0);
        ticketMessage.setCreateTime(LocalDateTime.now());
        ticketMessageMapper.insert(ticketMessage);

        //5. AI处理工单
        //TODO: 调用AI处理工单  ticketId  UserId content (ticketCreateRequest.getDescription())
        try{
            String reply = aiService.processTicket(ticket.getId(), ticketCreateRequest.getDescription(), userId);
            TicketMessage aiMsg = new TicketMessage();
            aiMsg.setTicketId(ticket.getId());
            aiMsg.setUserId(userId);
            aiMsg.setSenderType("AI");
            aiMsg.setMessageType("AI_REPLY");
            aiMsg.setContent(reply);
            aiMsg.setDeleted(0);
            aiMsg.setCreateTime(LocalDateTime.now());
            ticketMessageMapper.insert(aiMsg);
            ticket.setStatus("MANUAL_REVIEW");
        } catch (Exception e) {
            log.error("AI processing failed for ticket {}", ticket.getId(), e);
            ticket.setStatus("MANUAL_REVIEW");
        }
        ticketMapper.updateById(ticket);
        //6. 返回工单信息
        return getTicketDetail(ticket.getId());
    }

    //添加工单消息/沟通消息
    public void addTicketMessage(Long ticketId, MessageRequest messageRequest){
        //1.拿到当前工单信息
        Ticket ticket = ticketMapper.selectById(ticketId);
        if(ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())){
            throw new ApiException(HttpStatus.NOT_FOUND, "该工单不存在");
        }

        //2.拿到当前登录用户ID
        SysUser sysUser = userService.requireCurrentUser();
        Long userId = sysUser.getId();
        assertCanSendMessage(ticket, sysUser);
        //发送者类型，就是发送消息用户的类型
        String senderType = sysUser.getRole();

        //3.创建消息
        TicketMessage ticketMessage = new TicketMessage();
        ticketMessage.setTicketId(ticket.getId());
        ticketMessage.setUserId(sysUser.getId());
        ticketMessage.setSenderType(senderType);
        ticketMessage.setMessageType("TEXT");
        ticketMessage.setContent(messageRequest.getContent());
        ticketMessage.setDeleted(0);
        ticketMessage.setCreateTime(LocalDateTime.now());
        ticketMessageMapper.insert(ticketMessage);

        //发送者是用户的话，则需要AI处理
        if("USER".equals(senderType)){

            //TODO: 调用AI处理工单
            try{
                String reply = aiService.processTicket(ticket.getId(), messageRequest.getContent(), userId);
                TicketMessage aiMsg = new TicketMessage();
                aiMsg.setTicketId(ticket.getId());
                aiMsg.setUserId(userId);
                aiMsg.setSenderType("AI");
                aiMsg.setMessageType("AI_REPLY");
                aiMsg.setContent(reply);
                aiMsg.setDeleted(0);
                aiMsg.setCreateTime(LocalDateTime.now());
                ticketMessageMapper.insert(aiMsg);
            } catch (Exception e) {
                log.error("AI processing failed for ticket {}", ticket.getId(), e);
            }
            ticket.setStatus("AI_PROCESSING");
            ticketMapper.updateById(ticket);
        }

        //发送者是客服和管理，则工单状态变为 人工复核
        if("AGENT".equals(senderType) || "ADMIN".equals(senderType)){
            ticket.setStatus("MANUAL_REVIEW");
            ticketMapper.updateById(ticket);
        }

    }

    //修改工单的状态
    public void updateTicketStatus(Long ticketId, TicketUpdateRequest ticketUpdateRequest){
        //1.拿到当前工单信息
        Ticket ticket = ticketMapper.selectById(ticketId);
        if(ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())){
            throw new ApiException(HttpStatus.NOT_FOUND, "该工单不存在");
        }

        SysUser currentUser = userService.requireCurrentUser();
        assertCanUpdate(ticket, currentUser, ticketUpdateRequest);

        if(ticketUpdateRequest.getStatus() != null && !ticketUpdateRequest.getStatus().isBlank()){
            ticket.setStatus(ticketUpdateRequest.getStatus());
        }

        if(ticketUpdateRequest.getCategory() != null && !ticketUpdateRequest.getCategory().isBlank()){
            ticket.setCategory(ticketUpdateRequest.getCategory());
        }

        if(ticketUpdateRequest.getPriority() != null && !ticketUpdateRequest.getPriority().isBlank()){
            ticket.setPriority(ticketUpdateRequest.getPriority());
        }

        if(ticketUpdateRequest.getAgentId() != null && ticketUpdateRequest.getAgentId() != 0){
            ticket.setAgentId(ticketUpdateRequest.getAgentId());
        }
        ticketMapper.updateById(ticket);
    }

    private void assertCanView(Ticket ticket, SysUser currentUser) {
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }
        if ("USER".equals(currentUser.getRole()) && ticket.getUserId().equals(currentUser.getId())) {
            return;
        }
        if ("AGENT".equals(currentUser.getRole())
                && (currentUser.getId().equals(ticket.getAgentId())
                || (ticket.getAgentId() == null && "MANUAL_REVIEW".equals(ticket.getStatus())))) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "无权访问该工单");
    }

    private void assertCanSendMessage(Ticket ticket, SysUser currentUser) {
        if ("CLOSED".equals(ticket.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "已关闭工单不能继续发送消息");
        }
        if ("USER".equals(currentUser.getRole()) && ticket.getUserId().equals(currentUser.getId())) {
            return;
        }
        if ("AGENT".equals(currentUser.getRole()) && currentUser.getId().equals(ticket.getAgentId())) {
            return;
        }
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "无权向该工单发送消息");
    }

    private void assertCanUpdate(Ticket ticket, SysUser currentUser, TicketUpdateRequest request) {
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }
        if (!"AGENT".equals(currentUser.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前角色无权更新工单");
        }
        if (ticket.getAgentId() != null && !ticket.getAgentId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "该工单已由其他客服处理");
        }
        if (request.getAgentId() != null && !request.getAgentId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "客服只能将工单分配给自己");
        }
    }
}
