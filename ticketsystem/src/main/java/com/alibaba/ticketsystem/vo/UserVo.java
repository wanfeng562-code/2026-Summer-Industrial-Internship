package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserVo {

    private Long userId;

    private String username;

    private String nickname;

    private String token;

    private List<String> roles;

    private List<String> permissions;
}
