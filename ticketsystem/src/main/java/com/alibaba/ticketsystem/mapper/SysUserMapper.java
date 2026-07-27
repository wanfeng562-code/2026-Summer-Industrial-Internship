package com.alibaba.ticketsystem.mapper;

import com.alibaba.ticketsystem.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    //根据用户名获取用户的信息

    @Select("select * from sys_user where username = #{username}")
    public SysUser getUserByUsername(@Param("username") String username);

}
