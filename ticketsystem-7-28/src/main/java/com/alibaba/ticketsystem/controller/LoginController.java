package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.service.UserService;
import com.alibaba.ticketsystem.utils.R;
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

    @GetMapping("/user/logout")
    public R<?> logout() {
        StpUtil.logout();
        return R.success("用户成功退出系统");
    }

}
