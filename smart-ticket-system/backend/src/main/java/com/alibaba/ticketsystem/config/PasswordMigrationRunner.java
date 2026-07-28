package com.alibaba.ticketsystem.config;

import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 将样例数据中的明文密码迁移为 BCrypt，保证演示账号可继续登录。
 */
@Slf4j
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;

    public PasswordMigrationRunner(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public void run(String... args) {
        List<SysUser> users = sysUserMapper.selectList(new QueryWrapper<>());
        int migrated = 0;
        for (SysUser user : users) {
            if (user.getPassword() != null && !PasswordUtil.isBcrypt(user.getPassword())) {
                user.setPassword(PasswordUtil.encode(user.getPassword()));
                sysUserMapper.updateById(user);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("已将 {} 个用户密码迁移为 BCrypt", migrated);
        }
    }
}
