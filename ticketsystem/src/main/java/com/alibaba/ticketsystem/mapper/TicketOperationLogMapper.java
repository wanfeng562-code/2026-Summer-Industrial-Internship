package com.alibaba.ticketsystem.mapper;

import com.alibaba.ticketsystem.entity.TicketOperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TicketOperationLogMapper extends BaseMapper<TicketOperationLog> {

    @Select("""
            SELECT * FROM ticket_operation_log
            WHERE ticket_id = #{ticketId}
            ORDER BY create_time ASC, id ASC
            """)
    List<TicketOperationLog> selectByTicketId(@Param("ticketId") Long ticketId);
}
