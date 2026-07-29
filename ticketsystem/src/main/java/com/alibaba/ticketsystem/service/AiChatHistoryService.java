package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.AiChatMessage;
import com.alibaba.ticketsystem.entity.AiChatSession;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.AiChatMessageMapper;
import com.alibaba.ticketsystem.mapper.AiChatSessionMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiChatHistoryService {
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final UserService userService;

    @Transactional
    public AiChatSession recordUserMessage(String sessionNo, String content) {
        SysUser user = userService.requireCurrentUser();
        AiChatSession session = resolveOwnedSession(sessionNo, user, content);
        insertMessage(session.getId(), "USER", content);
        return session;
    }

    @Transactional
    public void recordAssistantMessage(Long sessionId, String content) {
        insertMessage(sessionId, "AI", content);
    }

    public List<AiChatSession> sessions() {
        SysUser user = userService.requireCurrentUser();
        return sessionMapper.selectList(new QueryWrapper<AiChatSession>()
                .eq("user_id", user.getId()).orderByDesc("update_time"));
    }

    public List<AiChatMessage> messages(String sessionNo) {
        SysUser user = userService.requireCurrentUser();
        AiChatSession session = requireOwnedSession(sessionNo, user.getId());
        return messageMapper.selectList(new QueryWrapper<AiChatMessage>()
                .eq("session_id", session.getId()).orderByAsc("id"));
    }

    /**
     * 将同一会话最近的消息作为模型上下文。只从已经通过当前用户归属校验的会话读取，
     * 避免页面虽然恢复了历史，但模型仍把每条消息当成全新对话。
     */
    public String buildModelPrompt(String sessionNo) {
        List<AiChatMessage> history = messages(sessionNo);
        int fromIndex = Math.max(0, history.size() - 12);
        StringBuilder prompt = new StringBuilder("""
                以下是同一客服会话最近的对话记录。请结合上下文回答最后一条用户消息。
                当问题涉及订单、工单或账号数据时，必须调用只读工具获取真实结果，不得根据历史回复猜测。

                """);
        history.subList(fromIndex, history.size()).forEach(message -> prompt
                .append("USER".equals(message.getSenderType()) ? "用户：" : "助手：")
                .append(message.getContent())
                .append('\n'));
        return prompt.toString();
    }

    private AiChatSession resolveOwnedSession(String sessionNo, SysUser user, String firstMessage) {
        if (sessionNo != null && !sessionNo.isBlank()) {
            return requireOwnedSession(sessionNo, user.getId());
        }
        AiChatSession session = new AiChatSession();
        session.setSessionNo(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(user.getId());
        session.setTitle(firstMessage.substring(0, Math.min(40, firstMessage.length())));
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    private AiChatSession requireOwnedSession(String sessionNo, Long userId) {
        AiChatSession session = sessionMapper.selectOne(new QueryWrapper<AiChatSession>()
                .eq("session_no", sessionNo).eq("user_id", userId));
        if (session == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "AI 会话不存在或无权访问");
        }
        return session;
    }

    private void insertMessage(Long sessionId, String senderType, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
    }
}
