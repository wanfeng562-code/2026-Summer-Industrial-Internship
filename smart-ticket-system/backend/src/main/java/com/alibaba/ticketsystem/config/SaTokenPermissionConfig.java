package com.alibaba.ticketsystem.config;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 Sa-Token 权限：按 USER / AGENT / ADMIN 分发角色与权限。
 */
@Component
public class SaTokenPermissionConfig implements StpInterface {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissions = new ArrayList<>();
        Long userId = Long.valueOf(loginId.toString());
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null || sysUser.getRole() == null) {
            return permissions;
        }
        String role = sysUser.getRole();
        if ("USER".equals(role)) {
            permissions.add("ticket:query");
            permissions.add("ticket:add");
            permissions.add("order:query");
            permissions.add("user:profile");
        }
        if ("AGENT".equals(role)) {
            permissions.add("ticket:query");
            permissions.add("ticket:add");
            permissions.add("ticket:update");
            permissions.add("order:query");
            permissions.add("user:profile");
        }
        if ("ADMIN".equals(role)) {
            permissions.add("ticket:query");
            permissions.add("ticket:add");
            permissions.add("ticket:update");
            permissions.add("order:query");
            permissions.add("user:profile");
            permissions.add("user:manage");
            permissions.add("policy:manage");
            permissions.add("stats:query");
        }
        return permissions;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        Long userId = Long.valueOf(loginId.toString());
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser != null && sysUser.getRole() != null) {
            roles.add(sysUser.getRole());
        }
        return roles;
    }
}
