package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.TicketMessage;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketMessageMapper;
import com.alibaba.ticketsystem.vo.TicketMessageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketMessageService {

    private final TicketMessageMapper ticketMessageMapper;
    private final SysUserMapper userMapper;

    public List<TicketMessageVo> getTicketMessageList(Long ticketId){
        List<TicketMessage> msgs = ticketMessageMapper.selectTicketMessageByTicketId(ticketId);
        List<TicketMessageVo> msgVos = new ArrayList<>();
        for (TicketMessage msg : msgs){
            TicketMessageVo msgVo = new TicketMessageVo();
            msgVo.setId(msg.getId());
            msgVo.setTicketId(msg.getTicketId());
            msgVo.setUserId(msg.getUserId());
            msgVo.setSenderType(msg.getSenderType());
            msgVo.setMessageType(msg.getMessageType());
            msgVo.setContent(msg.getContent());
            msgVo.setAiProcessResult(msg.getAiProcessResult());
            msgVo.setHumanFeedback(msg.getHumanFeedback());
            msgVo.setCreateTime(msg.getCreateTime());
            msgVo.setSenderName(resolveSenderName(msg));
            msgVos.add(msgVo);
        }
        return msgVos;
    }

    private String resolveSenderName(TicketMessage message) {
        if ("AI".equals(message.getSenderType())) {
            return "AI客服（小智）";
        }
        if ("SYSTEM".equals(message.getSenderType())) {
            return "系统";
        }
        SysUser sender = userMapper.selectById(message.getUserId());
        return sender == null ? "未知用户" : sender.getNickname();
    }
}
