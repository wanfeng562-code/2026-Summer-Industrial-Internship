package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.dto.ProfileUpdateRequest;
import com.alibaba.ticketsystem.dto.RegisterRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.vo.UserProfileVo;
import com.alibaba.ticketsystem.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionService authSessionService;

    @Transactional
    public UserVo loginSysUser(LoginRequest loginRequest){
        SysUser sysUser = sysUserMapper.getUserByUsername(loginRequest.getUsername());
        if(sysUser == null || !passwordMatches(loginRequest.getPassword(), sysUser)){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        // 兼容旧数据库中的演示明文密码：首次成功登录后立即升级为 BCrypt。
        if (!isBcryptHash(sysUser.getPassword())) {
            sysUser.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
            sysUser.setUpdateTime(LocalDateTime.now());
            sysUserMapper.updateById(sysUser);
        }

        authSessionService.login(sysUser.getId());
        UserVo userVo = new UserVo();
        userVo.setId(sysUser.getId());
        userVo.setUserId(sysUser.getId());
        userVo.setUsername(sysUser.getUsername());
        userVo.setNickname(sysUser.getNickname());
        userVo.setToken(authSessionService.getTokenValue());
        userVo.setRole(sysUser.getRole());
        userVo.setRoles(authSessionService.getRoleList());
        userVo.setPermissions(authSessionService.getPermissionList());

        return userVo;
    }

    public void logout() {
        authSessionService.logout();
    }

    @Transactional
    public UserProfileVo register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (sysUserMapper.usernameExists(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setEmail(normalizeNullable(request.getEmail()));
        user.setPhone(normalizeNullable(request.getPhone()));
        user.setRole("USER");
        user.setReputationScore(100);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        return toProfileVo(user);
    }

    public UserProfileVo getCurrentProfile() {
        return toProfileVo(requireCurrentUser());
    }

    @Transactional
    public UserProfileVo updateCurrentProfile(ProfileUpdateRequest request) {
        SysUser user = requireCurrentUser();
        user.setNickname(request.getNickname().trim());
        user.setEmail(normalizeNullable(request.getEmail()));
        user.setPhone(normalizeNullable(request.getPhone()));
        user.setAvatar(normalizeNullable(request.getAvatar()));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return toProfileVo(user);
    }

    public Page<UserProfileVo> pageUsers(int current, int size, String role) {
        QueryWrapper<SysUser> query = new QueryWrapper<SysUser>()
                .eq("deleted", 0)
                .orderByDesc("id");
        if (StringUtils.hasText(role)) {
            String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
            if (!List.of("USER", "AGENT", "ADMIN").contains(normalizedRole)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "角色参数不正确");
            }
            query.eq("role", normalizedRole);
        }

        Page<SysUser> userPage = sysUserMapper.selectPage(new Page<>(current, size), query);
        Page<UserProfileVo> result = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        result.setRecords(userPage.getRecords().stream().map(this::toProfileVo).toList());
        return result;
    }

    public SysUser requireCurrentUser() {
        Long userId = authSessionService.getCurrentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前登录用户不可用");
        }
        return user;
    }

    private boolean passwordMatches(String rawPassword, SysUser user) {
        String storedPassword = user.getPassword();
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.matches("^\\$2[ayb]\\$\\d{2}\\$.{53}$");
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private UserProfileVo toProfileVo(SysUser user) {
        UserProfileVo vo = new UserProfileVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setReputationScore(user.getReputationScore());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
