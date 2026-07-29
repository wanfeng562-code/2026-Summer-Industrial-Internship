package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_group")
public class AgentGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupName;
    private Long leaderId;
    private String description;
    private Integer enabled;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
