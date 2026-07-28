package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.dto.ProfileUpdateRequest;
import com.alibaba.ticketsystem.dto.RegisterRequest;
import com.alibaba.ticketsystem.service.UserService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/login")
    public R<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserVo userVo = userService.loginSysUser(loginRequest);
        return R.success("用户登录成功", userVo);
    }

    @PostMapping("/user/register")
    public R<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserVo userVo = userService.register(registerRequest);
        return R.success("注册成功", userVo);
    }

    @GetMapping("/user/logout")
    public R<?> logout() {
        StpUtil.logout();
        return R.success("用户成功退出系统");
    }

    @GetMapping("/user/profile")
    public R<?> profile() {
        return R.success("查询个人资料成功", userService.getProfile());
    }

    @PutMapping("/user/profile")
    public R<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return R.success("更新个人资料成功", userService.updateProfile(request));
    }
}
