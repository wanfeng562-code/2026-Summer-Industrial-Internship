package com.alibaba.ticketsystem.mapper;

import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.vo.TicketVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 工单表 Mapper 接口
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
public interface TicketMapper extends BaseMapper<Ticket> {


    @Select("SELECT t.*, u.username AS username, a.username AS agentName FROM ticket t " +
            "LEFT JOIN sys_user u ON t.user_id=u.id " +
            "LEFT JOIN sys_user a ON t.agent_id = a.id " +
            "WHERE t.deleted=0")
    public Page<TicketVo>  pageTicketVo(Page<TicketVo> page);

}
