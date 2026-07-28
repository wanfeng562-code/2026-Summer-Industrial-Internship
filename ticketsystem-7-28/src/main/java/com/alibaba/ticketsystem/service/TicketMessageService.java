package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.TicketMessage;
import com.alibaba.ticketsystem.mapper.TicketMessageMapper;
import com.alibaba.ticketsystem.vo.TicketMessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketMessageService {

    @Autowired
    private TicketMessageMapper ticketMessageMapper;

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
            msgVos.add(msgVo);
        }
        return msgVos;
    }
}
