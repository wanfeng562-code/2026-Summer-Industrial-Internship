package com.alibaba.ticketsystem.service;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对 Sa-Token 静态 API 进行统一封装，便于业务层测试和后续替换认证实现。
 */
@Service
public class AuthSessionService {

    public void login(Long userId) {
        StpUtil.login(userId);
    }

    public void logout() {
        StpUtil.logout();
    }

    public Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    public String getTokenValue() {
        return StpUtil.getTokenValue();
    }

    public List<String> getRoleList() {
        return StpUtil.getRoleList();
    }

    public List<String> getPermissionList() {
        return StpUtil.getPermissionList();
    }
}
