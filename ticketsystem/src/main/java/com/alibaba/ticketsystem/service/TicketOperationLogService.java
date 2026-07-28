package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.TicketOperationLog;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketOperationLogMapper;
import com.alibaba.ticketsystem.vo.TicketOperationLogVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketOperationLogService {

    private final TicketOperationLogMapper logMapper;
    private final SysUserMapper userMapper;

    public void record(Long ticketId,
                       String action,
                       Long operatorId,
                       String operatorRole,
                       String beforeStatus,
                       String afterStatus,
                       String detail) {
        TicketOperationLog log = new TicketOperationLog();
        log.setTicketId(ticketId);
        log.setAction(action);
        log.setOperatorId(operatorId);
        log.setOperatorRole(operatorRole);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setDetail(detail);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    public List<TicketOperationLogVo> list(Long ticketId) {
        return logMapper.selectByTicketId(ticketId).stream().map(this::toVo).toList();
    }

    private TicketOperationLogVo toVo(TicketOperationLog log) {
        TicketOperationLogVo vo = new TicketOperationLogVo();
        vo.setId(log.getId());
        vo.setTicketId(log.getTicketId());
        vo.setAction(log.getAction());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorRole(log.getOperatorRole());
        vo.setBeforeStatus(log.getBeforeStatus());
        vo.setAfterStatus(log.getAfterStatus());
        vo.setDetail(log.getDetail());
        vo.setCreateTime(log.getCreateTime());
        if (log.getOperatorId() != null) {
            SysUser operator = userMapper.selectById(log.getOperatorId());
            if (operator != null) {
                vo.setOperatorName(operator.getNickname());
            }
        } else {
            vo.setOperatorName("系统");
        }
        return vo;
    }
}
