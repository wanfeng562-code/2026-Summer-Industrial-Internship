package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")  //对应数据库表的名称
public class SysUser {

    @TableId(type = IdType.AUTO)   //说明主键   IdType.AUTO自增长
    private Long id;

    @TableField("username")   //当实体类的属性名和字段不一致时用的
    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private String role;

    private Integer reputationScore;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
