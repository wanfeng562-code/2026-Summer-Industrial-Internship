package com.alibaba.ticketsystem.service;
import java.time.LocalDateTime;

import com.alibaba.ticketsystem.domain.TicketStatus;
import com.alibaba.ticketsystem.dto.MessageRequest;
import com.alibaba.ticketsystem.dto.TicketCreateRequest;
import com.alibaba.ticketsystem.entity.Orders;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.TicketCategory;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketMapper ticketMapper;
    private final SysUserMapper sysUserMapper;
    private final OrdersMapper ordersMapper;
    private final TicketMessageMapper ticketMessageMapper;
    private final TicketMessageService ticketMessageService;
    private final AIService aiService;
    private final OrdersService ordersService;
    private final UserService userService;
    private final TicketOperationLogService operationLogService;
    private final AfterSalePolicyService policyService;
    private final AiProcessLogService aiProcessLogService;
    private final TicketCategoryService categoryService;
    private final AgentGroupService groupService;
    private final ContentModerationService contentModerationService;

    /** 工单状态中文名称映射 */
    private static final Map<String, String> STATUS_NAMES = Map.of(
            "PENDING", "待处理", "AI_PROCESSING", "AI预处理中",
            "MANUAL_REVIEW", "人工复核", "RESOLVED", "已解决", "REJECTED", "已驳回", "CLOSED", "已关闭"
    );

    /** 工单分类中文名称映射 */
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "REFUND", "退款退货", "LOGISTICS", "物流异常",
            "DAMAGE", "商品破损", "INVOICE", "发票问题", "OTHER", "其他"
    );

    //工单详情
    public TicketVo getTicketDetail(Long ticketId){
        Ticket ticket = requireViewableTicket(ticketId);
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
        ticketVo.setCategoryName(resolveCategoryName(ticket.getCategory()));
        ticketVo.setGroupId(ticket.getGroupId());
        ticketVo.setArchived(ticket.getArchived());
        ticketVo.setArchiveTime(ticket.getArchiveTime());
        ticketVo.setStatus(ticket.getStatus());
        ticketVo.setStatusName(STATUS_NAMES.get(ticket.getStatus()));
        ticketVo.setPriority(ticket.getPriority());
        ticketVo.setSlaWarning(ticket.getSlaWarning());
        ticketVo.setSlaEscalated(ticket.getSlaEscalated());
        ticketVo.setSlaDeadline(ticket.getSlaDeadline());
        ticketVo.setResolveTime(ticket.getResolveTime());
        ticketVo.setCloseTime(ticket.getCloseTime());
        ticketVo.setCreateTime(ticket.getCreateTime());
        ticketVo.setUpdateTime(ticket.getUpdateTime());
        ticketVo.setMessages(new ArrayList<TicketMessageVo>());

        SysUser user = sysUserMapper.selectById(ticket.getUserId());
        if(user != null){
            ticketVo.setUsername(user.getUsername());
            ticketVo.setUserNickname(user.getNickname());
        }

        if (ticket.getAgentId() != null) {
            SysUser agent = sysUserMapper.selectById(ticket.getAgentId());
            if(agent != null){
                ticketVo.setAgentName(agent.getNickname());
            }
        }

        if (ticket.getGroupId() != null) {
            try {
                ticketVo.setGroupName(groupService.require(ticket.getGroupId()).getGroupName());
            } catch (ApiException ignored) {
                // 历史工单关联的分组被删除时，仍保留其余详情内容。
            }
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
    public Page<TicketVo> pageTickets(int current, int size, String keyword, String status,
                                      String category, String priority, boolean archived){
        SysUser user = userService.requireCurrentUser();
        Page<TicketVo> page = new Page<>(current, size);
        return ticketMapper.pageTicketVo(page, user.getId(), user.getRole(), user.getAgentGroupId(),
                normalizeFilter(keyword), normalizeFilter(status), normalizeFilter(category),
                normalizeFilter(priority), archived ? 1 : 0);
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public List<TicketVo> exportTickets(String keyword, String status, String category,
                                        String priority, boolean archived) {
        SysUser user = userService.requireCurrentUser();
        if (!"ADMIN".equals(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "仅管理员可以导出工单");
        }
        Page<TicketVo> page = new Page<>(1, 100000);
        return ticketMapper.pageTicketVo(page, user.getId(), user.getRole(), user.getAgentGroupId(),
                normalizeFilter(keyword), normalizeFilter(status), normalizeFilter(category),
                normalizeFilter(priority), archived ? 1 : 0).getRecords();
    }

    @Transactional   //这是一个事务
    public TicketVo createTicket(TicketCreateRequest ticketCreateRequest){
        contentModerationService.validateUserContent(ticketCreateRequest.getTitle());
        contentModerationService.validateUserContent(ticketCreateRequest.getDescription());


        //1.把当前工单对应的订单信息拿到
        Orders orders = ordersService.requireOwnedOrderForTicket(ticketCreateRequest.getOrderId());

        SysUser currentUser = userService.requireCurrentUser();
        Long userId = currentUser.getId();

        //2.如果问题分类为空，则AI判断问题的类型
        String category = ticketCreateRequest.getCategory();
        boolean aiClassified = category == null || category.isBlank();
        if(category == null || category.isBlank()){
            //调用AI，用description判断问题类型，等接入AI再完成
            category = aiService.classify(ticketCreateRequest.getDescription());
            if (category == null || !CATEGORY_NAMES.containsKey(category)) {
                category = "OTHER";
            }
        }
        TicketCategory categoryConfig;
        try {
            categoryConfig = categoryService.requireActive(category);
        } catch (ApiException exception) {
            if (!aiClassified) {
                throw exception;
            }
            categoryConfig = categoryService.requireActive("OTHER");
        }
        category = categoryConfig.getCategoryCode();

        //3.创建工单
        Ticket ticket = new Ticket();
        ticket.setTicketNo("TK" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        ticket.setUserId(userId);
        ticket.setOrderId(orders.getId());
        ticket.setTitle(ticketCreateRequest.getTitle());
        ticket.setDescription(ticketCreateRequest.getDescription());
        ticket.setCategory(category);
        ticket.setGroupId(categoryConfig.getGroupId());
        if (categoryConfig.getGroupId() != null) {
            groupService.requireActive(categoryConfig.getGroupId());
        }
        ticket.setStatus(TicketStatus.AI_PROCESSING.name());
        String priority = ticketCreateRequest.getPriority();
        priority = priority == null || priority.isBlank() ? "MEDIUM" : priority;
        ticket.setPriority(priority);
        ticket.setSlaWarning(0);
        ticket.setSlaEscalated(0);
        LocalDateTime createTime = LocalDateTime.now();
        int slaHours = policyService.resolveSlaHours(
                category, orders.getTotalAmount(), currentUser.getReputationScore(), priority);
        ticket.setSlaDeadline(createTime.plusHours(slaHours));
        ticket.setDeleted(0);
        ticket.setCreateTime(createTime);
        ticket.setUpdateTime(createTime);
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
        operationLogService.record(ticket.getId(), "CREATE", userId, currentUser.getRole(),
                null, TicketStatus.AI_PROCESSING.name(),
                "用户创建工单，SLA=" + slaHours + "小时");

        //5. 通过已鉴权业务上下文调用 AI；失败时自动转人工。
        long aiStartedAt = System.currentTimeMillis();
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
            aiProcessLogService.record(ticket.getId(), aiMsg.getId(), category,
                    "AUTO_REPLY", reply, "创建工单后生成受控AI回复", aiStartedAt);
            operationLogService.record(ticket.getId(), "AI_REPLY", null, "SYSTEM",
                    TicketStatus.AI_PROCESSING.name(), TicketStatus.AI_PROCESSING.name(),
                    "AI已生成回复并保存");
        } catch (Exception e) {
            log.error("AI processing failed for ticket {}", ticket.getId(), e);
            aiProcessLogService.record(ticket.getId(), null, category,
                    "TRANSFER_MANUAL", null, "AI服务不可用", aiStartedAt);
            if (ticketMapper.transitionStatus(ticket.getId(), TicketStatus.AI_PROCESSING.name(),
                    TicketStatus.MANUAL_REVIEW.name(), null, null) == 1) {
                operationLogService.record(ticket.getId(), "AI_TRANSFER_MANUAL", null, "SYSTEM",
                        TicketStatus.AI_PROCESSING.name(), TicketStatus.MANUAL_REVIEW.name(),
                        "AI服务不可用，自动转人工");
            }
        }
        //6. 返回工单信息
        return getTicketDetail(ticket.getId());
    }

    //添加工单消息/沟通消息
    @Transactional
    public void addTicketMessage(Long ticketId, MessageRequest messageRequest){
        contentModerationService.validateUserContent(messageRequest.getContent());
        //1.拿到当前工单信息
        Ticket ticket = ticketMapper.selectById(ticketId);
        if(ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())){
            throw new ApiException(HttpStatus.NOT_FOUND, "该工单不存在");
        }
        if (Integer.valueOf(1).equals(ticket.getArchived())) {
            throw new ApiException(HttpStatus.CONFLICT, "已归档工单不能继续发送消息");
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
        String beforeStatus = ticket.getStatus();
        String afterStatus = beforeStatus;

        if ("USER".equals(senderType) && TicketStatus.RESOLVED.name().equals(beforeStatus)) {
            if (ticketMapper.transitionStatus(ticketId, TicketStatus.RESOLVED.name(),
                    TicketStatus.MANUAL_REVIEW.name(), null, null) != 1) {
                throw new ApiException(HttpStatus.CONFLICT, "工单状态已变化，请刷新后重试");
            }
            afterStatus = TicketStatus.MANUAL_REVIEW.name();
        }

        // 只有仍处于 AI 处理阶段的用户消息才触发 AI；人工阶段由已接单客服处理。
        if ("USER".equals(senderType) && TicketStatus.AI_PROCESSING.name().equals(beforeStatus)) {
            long aiStartedAt = System.currentTimeMillis();
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
                aiProcessLogService.record(ticket.getId(), aiMsg.getId(), ticket.getCategory(),
                        "AUTO_REPLY", reply, "用户补充后生成受控AI回复", aiStartedAt);
                operationLogService.record(ticket.getId(), "AI_REPLY", null, "SYSTEM",
                        beforeStatus, beforeStatus, "AI已生成回复并保存");
            } catch (Exception e) {
                log.error("AI processing failed for ticket {}", ticket.getId(), e);
                aiProcessLogService.record(ticket.getId(), null, ticket.getCategory(),
                        "TRANSFER_MANUAL", null, "AI服务不可用", aiStartedAt);
                if (ticketMapper.transitionStatus(ticketId, TicketStatus.AI_PROCESSING.name(),
                        TicketStatus.MANUAL_REVIEW.name(), null, null) == 1) {
                    afterStatus = TicketStatus.MANUAL_REVIEW.name();
                    operationLogService.record(ticketId, "AI_TRANSFER_MANUAL", null, "SYSTEM",
                            beforeStatus, afterStatus, "AI服务不可用，自动转人工");
                }
            }
        }

        operationLogService.record(ticketId, "MESSAGE_ADD", userId, senderType,
                beforeStatus, afterStatus, "发送工单消息");
    }

    public List<TicketMessageVo> getAccessibleMessages(Long ticketId) {
        requireViewableTicket(ticketId);
        return ticketMessageService.getTicketMessageList(ticketId);
    }

    public Ticket requireViewableTicket(Long ticketId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || Integer.valueOf(1).equals(ticket.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "该工单不存在");
        }
        assertCanView(ticket, userService.requireCurrentUser());
        return ticket;
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
                || (currentUser.getAgentGroupId() != null
                && currentUser.getAgentGroupId().equals(ticket.getGroupId()))
                || (ticket.getAgentId() == null && ticket.getGroupId() == null
                && TicketStatus.MANUAL_REVIEW.name().equals(ticket.getStatus())))) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "无权访问该工单");
    }

    private void assertCanSendMessage(Ticket ticket, SysUser currentUser) {
        if ("CLOSED".equals(ticket.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "已关闭工单不能继续发送消息");
        }
        if (TicketStatus.REJECTED.name().equals(ticket.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "已驳回工单请通过“跟进”补充材料并重新进入人工复核");
        }
        if ("USER".equals(currentUser.getRole()) && ticket.getUserId().equals(currentUser.getId())) {
            return;
        }
        if ("AGENT".equals(currentUser.getRole())
                && TicketStatus.MANUAL_REVIEW.name().equals(ticket.getStatus())
                && currentUser.getId().equals(ticket.getAgentId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "无权向该工单发送消息");
    }

    private String resolveCategoryName(String categoryCode) {
        return categoryService.list(true).stream()
                .filter(item -> item.getCategoryCode().equals(categoryCode))
                .map(TicketCategory::getCategoryName)
                .findFirst()
                .orElse(CATEGORY_NAMES.getOrDefault(categoryCode, categoryCode));
    }

}
