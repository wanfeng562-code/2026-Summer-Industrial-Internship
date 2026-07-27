package com.alibaba.ticketsystem.config;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义Sa-Token权限验证接口
 * 说明：登录认证的用户有哪些角色，以及有哪些权限
 */
@Component
public class SaTokenPermissionConfig implements StpInterface {

    @Autowired
    private SysUserMapper sysUserMapper;

    //获取当前用户的权限列表
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissions = new ArrayList<>();
        // 根据登录用户ID，获取用户拥有的角色
        Long userId = Long.valueOf(loginId.toString());
        SysUser sysUser = sysUserMapper.selectById(userId);
        if(sysUser.getRole().equals("USER")){
            permissions.add("ticket:query");  //工单查询权限
            permissions.add("ticket:add");  //创建工单权限
        }
        if(sysUser.getRole().equals("AGENT")){
            permissions.add("ticket:query");  //工单查询权限
            permissions.add("ticket:add");  //创建工单权限
            permissions.add("ticket:update");  //修改工单权限
        }
        if(sysUser.getRole().equals("ADMIN")){
            permissions.add("ticket:query");  //工单查询权限
            permissions.add("ticket:add");  //创建工单权限
            permissions.add("ticket:update");  //修改工单权限
        }
        return permissions;  //返回权限的集合
    }

    //获取当前用户的角色
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        // 根据登录用户ID，获取用户拥有的角色
        Long userId = Long.valueOf(loginId.toString());
        SysUser sysUser = sysUserMapper.selectById(userId);
        roles.add(sysUser.getRole());  //拿到当前用户的角色
        return roles;  //返回角色的集合
    }
}
