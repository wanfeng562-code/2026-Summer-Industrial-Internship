package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.service.UserService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/login")
    public R<?> login(@Valid LoginRequest loginRequest) {
        UserVo userVo = userService.loginSysUser(loginRequest);
        return R.success("用户登录成功", userVo);
    }

    @GetMapping("/user/logout")
    public R<?> logout() {
        StpUtil.logout();
        return R.success("用户成功退出系统");
    }

}
