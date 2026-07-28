package com.alibaba.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("faq")
public class Faq implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String category;
    private String question;
    private String answer;
    private String keywords;
    private Integer enabled;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
