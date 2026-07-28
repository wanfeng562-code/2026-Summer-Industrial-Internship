package com.alibaba.ticketsystem;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.mapper.SysUserMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.service.TicketService;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketOwnershipTest {

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void userCannotReadOthersTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setUserId(2L);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRole("USER");
        Mockito.when(sysUserMapper.selectById(1L)).thenReturn(user);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            ApiException ex = Assertions.assertThrows(ApiException.class,
                    () -> ticketService.assertTicketReadable(ticket));
            Assertions.assertEquals(403, ex.getCode());
        }
    }

    @Test
    void agentCanReadAnyTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setUserId(2L);

        SysUser agent = new SysUser();
        agent.setId(3L);
        agent.setRole("AGENT");
        Mockito.when(sysUserMapper.selectById(3L)).thenReturn(agent);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(3L);
            Assertions.assertDoesNotThrow(() -> ticketService.assertTicketReadable(ticket));
        }
    }
}
