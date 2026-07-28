package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVo {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer reputationScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
