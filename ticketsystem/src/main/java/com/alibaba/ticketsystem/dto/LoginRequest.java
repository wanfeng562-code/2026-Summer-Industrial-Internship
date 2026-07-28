package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 16, message = "用户账户必须是3-16个字符")
    private String username;

    @NotBlank(message = "用户密码不能为空")
    @Size(min = 6, max = 32, message = "用户密码必须是6-32个字符")
    private String password;
}
