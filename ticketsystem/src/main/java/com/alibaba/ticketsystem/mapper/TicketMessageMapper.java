package com.alibaba.ticketsystem.mapper;

import com.alibaba.ticketsystem.entity.TicketMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 工单消息表 Mapper 接口
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
public interface TicketMessageMapper extends BaseMapper<TicketMessage> {

    //根据工单ID查询该工单的消息集合
    @Select("SELECT * FROM ticket_message WHERE ticket_id=#{ticketId}")
    public List<TicketMessage> selectTicketMessageByTicketId(@Param("ticketId") Long ticketId);
}
