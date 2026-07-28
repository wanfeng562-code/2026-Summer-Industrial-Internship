package com.alibaba.ticketsystem.config;

import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SaTokenPermissionConfigTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SaTokenPermissionConfig config = new SaTokenPermissionConfig(userMapper);

    @Test
    void userHasOwnOrderAndTicketPermissionsOnly() {
        when(userMapper.selectById(4L)).thenReturn(user("USER"));

        assertThat(config.getRoleList(4L, "login")).containsExactly("USER");
        assertThat(config.getPermissionList(4L, "login"))
                .contains("order:query", "ticket:query", "ticket:add", "ticket:message", "faq:query")
                .doesNotContain("order:query:all", "ticket:update", "user:manage");
    }

    @Test
    void adminHasManagementAndAllOrderPermissions() {
        when(userMapper.selectById(1L)).thenReturn(user("ADMIN"));

        assertThat(config.getPermissionList(1L, "login"))
                .contains("order:query:all", "ticket:assign", "ticket:resolve",
                        "ticket:close", "user:manage", "policy:manage", "faq:manage");
    }

    @Test
    void deletedUserHasNoRoleOrPermission() {
        SysUser deletedUser = user("ADMIN");
        deletedUser.setDeleted(1);
        when(userMapper.selectById(1L)).thenReturn(deletedUser);

        assertThat(config.getRoleList(1L, "login")).isEmpty();
        assertThat(config.getPermissionList(1L, "login")).isEmpty();
    }

    private SysUser user(String role) {
        SysUser user = new SysUser();
        user.setRole(role);
        user.setDeleted(0);
        return user;
    }
}
