package com.alibaba.ticketsystem.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

    private String nickname;

    private String email;

    private String phone;

    /** 可选：修改密码时填写新密码 */
    private String password;
}
