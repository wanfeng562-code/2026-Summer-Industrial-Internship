package com.alibaba.ticketsystem.config;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 自定义Sa-Token权限验证接口
 * 说明：登录认证的用户有哪些角色，以及有哪些权限
 */
@Component
@RequiredArgsConstructor
public class SaTokenPermissionConfig implements StpInterface {

    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
            "USER", List.of("order:query", "ticket:query", "ticket:add", "ticket:message"),
            "AGENT", List.of("ticket:query", "ticket:update", "ticket:message", "ticket:claim"),
            "ADMIN", List.of(
                    "order:query", "order:query:all",
                    "ticket:query", "ticket:update", "ticket:message", "ticket:claim",
                    "user:manage", "policy:manage", "stats:query"
            )
    );

    private final SysUserMapper sysUserMapper;

    //获取当前用户的权限列表
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SysUser user = getActiveUser(loginId);
        return user == null ? List.of() : ROLE_PERMISSIONS.getOrDefault(user.getRole(), List.of());
    }

    //获取当前用户的角色
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = getActiveUser(loginId);
        return user == null ? List.of() : List.of(user.getRole());
    }

    private SysUser getActiveUser(Object loginId) {
        Long userId = Long.valueOf(loginId.toString());
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            return null;
        }
        return user;
    }
}
