package com.alibaba.ticketsystem.mapper;

import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.vo.TicketVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 工单表 Mapper 接口
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
public interface TicketMapper extends BaseMapper<Ticket> {


    @Select("""
            <script>
            SELECT t.*, u.username AS username, a.username AS agentName, g.group_name AS groupName FROM ticket t
            LEFT JOIN sys_user u ON t.user_id=u.id 
            LEFT JOIN sys_user a ON t.agent_id = a.id 
            LEFT JOIN agent_group g ON t.group_id = g.id
            <where>
                t.deleted=0 
                AND t.archived = #{archived}
                <if test="status != null and status != ''">
                    AND t.status = #{status}
                </if>
                <if test="category != null and category != ''">
                    AND t.category = #{category}
                </if>
                <if test="priority != null and priority != ''">
                    AND t.priority = #{priority}
                </if>
                <if test="keyword != null and keyword != ''">
                    AND (t.ticket_no LIKE CONCAT('%', #{keyword}, '%')
                         OR t.title LIKE CONCAT('%', #{keyword}, '%')
                         OR u.username LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <choose>
                    <when test='"USER".equals(role)'>
                        AND t.user_id = #{currentUserId}
                    </when>
                    <when test='"AGENT".equals(role)'>
                        AND (
                            t.agent_id = #{currentUserId}
                            OR (t.group_id = #{agentGroupId} AND t.status IN ('AI_PROCESSING', 'MANUAL_REVIEW'))
                            OR (t.agent_id IS NULL AND t.group_id IS NULL AND t.status = 'MANUAL_REVIEW')
                        )
                    </when>
                    <when test='"ADMIN".equals(role)'>
                        AND 1 = 1
                    </when>
                    <otherwise>
                        AND 1 = 0
                    </otherwise>
                </choose>
            </where>
            order by t.create_time desc
            </script>
            """)
    Page<TicketVo> pageTicketVo(Page<TicketVo> page,
                                @Param("currentUserId") Long currentUserId,
                                @Param("role") String role,
                                @Param("agentGroupId") Long agentGroupId,
                                @Param("keyword") String keyword,
                                @Param("status") String status,
                                @Param("category") String category,
                                @Param("priority") String priority,
                                @Param("archived") Integer archived);


    @Update("UPDATE ticket SET priority = #{priority}, update_time = NOW() " +
            "WHERE id = #{ticketId} AND deleted = 0 AND archived = 0")
    int updatePriorityById(@Param("ticketId") Long ticketId,
                           @Param("priority") String priority);

    @Update("""
            UPDATE ticket SET agent_id = #{agentId}, update_time = NOW()
            WHERE id = #{ticketId} AND deleted = 0
              AND status = 'MANUAL_REVIEW' AND agent_id IS NULL
            """)
    int claim(@Param("ticketId") Long ticketId, @Param("agentId") Long agentId);

    @Update("""
            UPDATE ticket SET agent_id = #{agentId}, update_time = NOW()
            WHERE id = #{ticketId} AND deleted = 0 AND status = 'MANUAL_REVIEW'
            """)
    int assignAgent(@Param("ticketId") Long ticketId, @Param("agentId") Long agentId);

    @Update("""
            UPDATE ticket
            SET status = #{toStatus},
                resolve_time = COALESCE(#{resolveTime}, resolve_time),
                close_time = COALESCE(#{closeTime}, close_time),
                update_time = NOW()
            WHERE id = #{ticketId} AND deleted = 0 AND status = #{fromStatus}
            """)
    int transitionStatus(@Param("ticketId") Long ticketId,
                         @Param("fromStatus") String fromStatus,
                         @Param("toStatus") String toStatus,
                         @Param("resolveTime") java.time.LocalDateTime resolveTime,
                         @Param("closeTime") java.time.LocalDateTime closeTime);

    @Update("UPDATE ticket SET archived = 1, archive_time = NOW(), update_time = NOW() " +
            "WHERE id = #{ticketId} AND deleted = 0 AND archived = 0 AND status IN ('CLOSED', 'REJECTED')")
    int archive(@Param("ticketId") Long ticketId);
}
