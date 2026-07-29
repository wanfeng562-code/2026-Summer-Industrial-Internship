package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.AgentGroupRequest;
import com.alibaba.ticketsystem.entity.AgentGroup;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.mapper.AgentGroupMapper;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketCategoryMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentGroupServiceTest {

    private final AgentGroupMapper groupMapper = mock(AgentGroupMapper.class);
    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final TicketCategoryMapper categoryMapper = mock(TicketCategoryMapper.class);
    private final TicketMapper ticketMapper = mock(TicketMapper.class);
    private final AgentGroupService service =
            new AgentGroupService(groupMapper, userMapper, categoryMapper, ticketMapper);

    @Test
    void agentCannotBeSilentlyMovedFromAnotherGroup() {
        SysUser agent = agent(2L, 99L);
        when(userMapper.selectById(2L)).thenReturn(agent);
        doAnswer(invocation -> {
            ((AgentGroup) invocation.getArgument(0)).setId(8L);
            return 1;
        }).when(groupMapper).insert(any(AgentGroup.class));
        AgentGroupRequest request = request();
        request.setAgentIds(List.of(2L));

        assertStatus(() -> service.create(request), HttpStatus.CONFLICT);
    }

    @Test
    void referencedGroupCannotBeDeleted() {
        when(groupMapper.selectById(8L)).thenReturn(group());
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        assertStatus(() -> service.delete(8L), HttpStatus.CONFLICT);
    }

    private AgentGroupRequest request() {
        AgentGroupRequest request = new AgentGroupRequest();
        request.setGroupName("测试组");
        request.setEnabled(1);
        return request;
    }

    private AgentGroup group() {
        AgentGroup group = new AgentGroup();
        group.setId(8L);
        group.setGroupName("测试组");
        group.setEnabled(1);
        group.setDeleted(0);
        return group;
    }

    private SysUser agent(Long id, Long groupId) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setNickname("客服");
        user.setRole("AGENT");
        user.setAgentGroupId(groupId);
        user.setDeleted(0);
        return user;
    }

    private void assertStatus(Runnable action, HttpStatus status) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(status));
    }
}
