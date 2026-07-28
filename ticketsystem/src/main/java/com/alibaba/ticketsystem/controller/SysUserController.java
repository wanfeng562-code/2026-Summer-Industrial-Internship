package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.ProfileUpdateRequest;
import com.alibaba.ticketsystem.service.UserService;
import com.alibaba.ticketsystem.utils.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author YanTao
 * @since 2026-07-17
 */
@Validated
@RestController
@RequiredArgsConstructor
public class SysUserController {

    private final UserService userService;

    @GetMapping("/user/profile")
    public R<?> getProfile() {
        return R.success("个人资料查询成功", userService.getCurrentProfile());
    }

    @PutMapping("/user/profile")
    public R<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return R.success("个人资料修改成功", userService.updateCurrentProfile(request));
    }

    @SaCheckPermission("user:manage")
    @GetMapping("/users")
    public R<?> pageUsers(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "current必须大于0") Integer current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size必须大于0")
            @Max(value = 100, message = "size不能超过100") Integer size,
            @RequestParam(required = false) String role) {
        return R.success("用户分页查询成功", userService.pageUsers(current, size, role));
    }
}
