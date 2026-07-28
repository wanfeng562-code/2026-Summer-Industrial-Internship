package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.AiProcessLog;
import com.alibaba.ticketsystem.mapper.AiProcessLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiProcessLogService {

    private final AiProcessLogMapper logMapper;

    public void record(Long ticketId,
                       Long messageId,
                       String intent,
                       String action,
                       String reply,
                       String detail,
                       long startedAtMillis) {
        AiProcessLog log = new AiProcessLog();
        log.setTicketId(ticketId);
        log.setMessageId(messageId);
        log.setIntentResult(intent);
        log.setAiAction(action);
        log.setAiReply(reply);
        log.setProcessDetail(detail);
        log.setExecutionTime((int) Math.min(Integer.MAX_VALUE,
                Math.max(0L, System.currentTimeMillis() - startedAtMillis)));
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }
}
