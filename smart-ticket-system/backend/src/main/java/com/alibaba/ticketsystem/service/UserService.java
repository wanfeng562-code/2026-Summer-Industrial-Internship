package com.alibaba.ticketsystem.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.dto.ProfileUpdateRequest;
import com.alibaba.ticketsystem.dto.RegisterRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.utils.PasswordUtil;
import com.alibaba.ticketsystem.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    public UserVo loginSysUser(LoginRequest loginRequest) {
        SysUser sysUser = sysUserMapper.getUserByUsername(loginRequest.getUsername());
        if (sysUser == null || !PasswordUtil.matches(loginRequest.getPassword(), sysUser.getPassword())) {
            throw new ApiException(401, "您的用户名或密码输入错误");
        }
        // 登录时顺带把明文密码升级为 BCrypt
        if (!PasswordUtil.isBcrypt(sysUser.getPassword())) {
            sysUser.setPassword(PasswordUtil.encode(loginRequest.getPassword()));
            sysUserMapper.updateById(sysUser);
        }
        StpUtil.login(sysUser.getId());
        return toUserVo(sysUser, true);
    }

    public UserVo register(RegisterRequest request) {
        SysUser exists = sysUserMapper.getUserByUsername(request.getUsername());
        if (exists != null) {
            throw new ApiException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setReputationScore(80);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        StpUtil.login(user.getId());
        return toUserVo(user, true);
    }

    public UserVo getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new ApiException(404, "用户不存在");
        }
        return toUserVo(sysUser, false);
    }

    public UserVo updateProfile(ProfileUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new ApiException(404, "用户不存在");
        }
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            sysUser.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            sysUser.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            sysUser.setPhone(request.getPhone());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            sysUser.setPassword(PasswordUtil.encode(request.getPassword()));
        }
        sysUser.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(sysUser);
        return toUserVo(sysUser, false);
    }

    private UserVo toUserVo(SysUser sysUser, boolean withToken) {
        UserVo userVo = new UserVo();
        userVo.setUserId(sysUser.getId());
        userVo.setUsername(sysUser.getUsername());
        userVo.setNickname(sysUser.getNickname());
        if (withToken) {
            userVo.setToken(StpUtil.getTokenValue());
        }
        userVo.setRoles(StpUtil.getRoleList());
        userVo.setPermissions(StpUtil.getPermissionList());
        return userVo;
    }
}
