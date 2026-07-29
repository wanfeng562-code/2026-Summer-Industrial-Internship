package com.alibaba.ticketsystem.controller;

import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.dto.RegisterRequest;
import com.alibaba.ticketsystem.service.UserService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.vo.UserProfileVo;
import com.alibaba.ticketsystem.vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin   //解决跨域异常的注解
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/login")  //@RequestBody 用来接收前端提交JSON对象
    public R<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserVo userVo = userService.loginSysUser(loginRequest);
        return R.success("用户登录成功", userVo);
    }

    @PostMapping("/user/register")
    public R<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserProfileVo user = userService.register(registerRequest);
        return R.success("用户注册成功", user);
    }

    @PostMapping("/user/logout")
    public R<?> logout() {
        userService.logout();
        return R.success("用户成功退出系统");
    }

}
