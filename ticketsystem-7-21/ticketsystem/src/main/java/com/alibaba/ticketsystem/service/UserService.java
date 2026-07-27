package com.alibaba.ticketsystem.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.dto.LoginRequest;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    public UserVo loginSysUser(LoginRequest loginRequest){
        SysUser sysUser = sysUserMapper.getUserByUsername(loginRequest.getUsername());
        if(sysUser == null){
            throw new ApiException("您的用户名或密码输入错误");
        }
        if(!sysUser.getPassword().equals(loginRequest.getPassword())){
            throw new ApiException("您的用户名或密码输入错误");
        }
        StpUtil.login(sysUser.getId());
        UserVo userVo = new UserVo();
        userVo.setUserId(sysUser.getId());
        userVo.setUsername(sysUser.getUsername());
        userVo.setNickname(sysUser.getNickname());
        userVo.setToken(StpUtil.getTokenValue());
        userVo.setRoles(StpUtil.getRoleList());
        userVo.setPermissions(StpUtil.getPermissionList());

        return userVo;
    }
}
