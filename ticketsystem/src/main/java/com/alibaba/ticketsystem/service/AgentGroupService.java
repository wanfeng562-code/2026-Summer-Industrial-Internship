package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.AgentGroupRequest;
import com.alibaba.ticketsystem.entity.AgentGroup;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketCategory;
import com.alibaba.ticketsystem.mapper.AgentGroupMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketCategoryMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentGroupService {

    private final AgentGroupMapper groupMapper;
    private final SysUserMapper userMapper;
    private final TicketCategoryMapper categoryMapper;
    private final TicketMapper ticketMapper;

    public List<AgentGroup> listActive() {
        return groupMapper.selectList(new QueryWrapper<AgentGroup>()
                .eq("deleted", 0).eq("enabled", 1).orderByAsc("id"));
    }

    public List<AgentGroup> listAll() {
        return groupMapper.selectList(new QueryWrapper<AgentGroup>()
                .eq("deleted", 0).orderByAsc("id"));
    }

    @Transactional
    public AgentGroup create(AgentGroupRequest request) {
        if (groupMapper.selectCount(new QueryWrapper<AgentGroup>()
                .eq("deleted", 0).eq("group_name", request.getGroupName().trim())) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "坐席组名称已存在");
        }
        AgentGroup group = new AgentGroup();
        apply(group, request);
        group.setDeleted(0);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.insert(group);
        replaceMembers(group.getId(), request.getLeaderId(), request.getAgentIds());
        return group;
    }

    @Transactional
    public AgentGroup update(Long id, AgentGroupRequest request) {
        AgentGroup group = require(id);
        if (Integer.valueOf(0).equals(request.getEnabled())
                && categoryMapper.selectCount(new QueryWrapper<TicketCategory>()
                        .eq("group_id", id).eq("enabled", 1).eq("deleted", 0)) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "该坐席组仍被启用分类引用，请先停用或调整分类");
        }
        if (groupMapper.selectCount(new QueryWrapper<AgentGroup>()
                .eq("deleted", 0).eq("group_name", request.getGroupName().trim()).ne("id", id)) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "坐席组名称已存在");
        }
        apply(group, request);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        replaceMembers(id, request.getLeaderId(), request.getAgentIds());
        return group;
    }

    @Transactional
    public void delete(Long id) {
        AgentGroup group = require(id);
        if (categoryMapper.selectCount(new QueryWrapper<TicketCategory>()
                .eq("group_id", id).eq("deleted", 0)) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "该坐席组仍被工单分类引用，不能删除");
        }
        if (ticketMapper.selectCount(new QueryWrapper<Ticket>()
                .eq("group_id", id).eq("deleted", 0)) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "该坐席组已有工单记录，不能删除，可改为停用");
        }
        group.setDeleted(1);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        userMapper.update(null, new UpdateWrapper<SysUser>()
                .eq("agent_group_id", id).set("agent_group_id", null));
    }

    public AgentGroup require(Long id) {
        AgentGroup group = groupMapper.selectById(id);
        if (group == null || Integer.valueOf(1).equals(group.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "坐席组不存在");
        }
        return group;
    }

    public AgentGroup requireActive(Long id) {
        AgentGroup group = require(id);
        if (!Integer.valueOf(1).equals(group.getEnabled())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "坐席组已被禁用");
        }
        return group;
    }

    private void apply(AgentGroup group, AgentGroupRequest request) {
        Long leaderId = request.getLeaderId();
        if (leaderId != null) {
            requireAgent(leaderId);
        }
        group.setGroupName(request.getGroupName().trim());
        group.setLeaderId(leaderId);
        group.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        group.setEnabled(request.getEnabled());
    }

    private void replaceMembers(Long groupId, Long leaderId, List<Long> agentIds) {
        List<Long> requestedIds = new ArrayList<>();
        if (leaderId != null) {
            requestedIds.add(leaderId);
        }
        if (agentIds != null) {
            requestedIds.addAll(agentIds);
        }
        requestedIds.stream().distinct().forEach(agentId -> requireAgentAvailable(agentId, groupId));

        userMapper.update(null, new UpdateWrapper<SysUser>()
                .eq("agent_group_id", groupId).set("agent_group_id", null));
        if (leaderId != null) {
            userMapper.update(null, new UpdateWrapper<SysUser>()
                    .eq("id", leaderId).set("agent_group_id", groupId));
        }
        if (agentIds == null) return;
        for (Long agentId : agentIds.stream().distinct().toList()) {
            requireAgent(agentId);
            userMapper.update(null, new UpdateWrapper<SysUser>()
                    .eq("id", agentId).set("agent_group_id", groupId));
        }
    }

    private void requireAgentAvailable(Long userId, Long groupId) {
        SysUser user = requireAgent(userId);
        if (user.getAgentGroupId() != null && !groupId.equals(user.getAgentGroupId())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "客服“" + user.getNickname() + "”已属于其他坐席组，请先从原组移除");
        }
    }

    private SysUser requireAgent(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted()) || !"AGENT".equals(user.getRole())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "坐席组负责人和成员必须是有效客服");
        }
        return user;
    }
}
