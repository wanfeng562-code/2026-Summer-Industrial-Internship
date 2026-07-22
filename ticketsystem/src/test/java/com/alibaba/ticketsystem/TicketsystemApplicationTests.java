package com.alibaba.ticketsystem;

import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class TicketsystemApplicationTests {

    @Autowired
    private SysUserMapper sysUserMapper;

    //根据ID去查询
    @Test
    public void test01() {
        SysUser sysUser = sysUserMapper.selectById(1);
        System.out.println(sysUser);
    }

    //根据条件去查询 或查询所有
    @Test
    public void test02() {
        //条件构造器  where
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "agent_zhang");
        List<SysUser> sysUsers = sysUserMapper.selectList(queryWrapper);
        for (SysUser sysUser : sysUsers) {
            System.out.println(sysUser);
        }
    }

    //添加一个用户
    @Test
    public void test03(){
        SysUser sysUser = new SysUser();
        sysUser.setUsername("user_ada");
        sysUser.setPassword("123456");
        sysUser.setNickname("Ada用户");
        sysUser.setEmail("ada@126.com");
        sysUser.setPhone("13599000099");
        sysUser.setRole("USER");
        sysUser.setReputationScore(100);
        sysUser.setDeleted(0);
        sysUser.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(sysUser);
    }

    @Test
    public void test04(){
        SysUser sysUser = new SysUser();
        sysUser.setId(7L);
        sysUser.setUsername("user_ada");
        sysUser.setPassword("123456");
        sysUser.setNickname("AdaWang");
        sysUser.setEmail("adawang@126.com");
        sysUser.setPhone("13500112233");
        sysUser.setRole("USER");
        sysUser.setReputationScore(100);
        sysUser.setDeleted(0);
        sysUser.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(sysUser);
    }

    //硬删除
    @Test
    public void test05(){
        sysUserMapper.deleteById(7L);
    }

    //软删除
    @Test
    public void test06(){
        SysUser sysUser = new SysUser();
        sysUser.setId(8L);
        sysUser.setDeleted(1);
        sysUserMapper.updateById(sysUser);
    }

}
