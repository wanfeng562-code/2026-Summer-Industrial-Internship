package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.AgentGroupRequest;
import com.alibaba.ticketsystem.entity.AgentGroup;
import com.alibaba.ticketsystem.service.AgentGroupService;
import com.alibaba.ticketsystem.service.UserService;
import com.alibaba.ticketsystem.utils.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 坐席分组与组长配置。 */
@RestController
@RequestMapping("/agent-groups")
@RequiredArgsConstructor
public class AgentGroupController {

    private final AgentGroupService agentGroupService;
    private final UserService userService;

    @GetMapping
    @SaCheckPermission("ticket:query")
    public R<List<AgentGroup>> list(@RequestParam(defaultValue = "false") boolean includeDisabled) {
        boolean canSeeDisabled = includeDisabled && "ADMIN".equals(userService.requireCurrentUser().getRole());
        return R.success("查询坐席分组成功", canSeeDisabled ? agentGroupService.listAll() : agentGroupService.listActive());
    }

    @PostMapping
    @SaCheckPermission("group:manage")
    public R<AgentGroup> create(@Valid @RequestBody AgentGroupRequest request) {
        return R.success("创建坐席分组成功", agentGroupService.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("group:manage")
    public R<AgentGroup> update(@PathVariable Long id, @Valid @RequestBody AgentGroupRequest request) {
        return R.success("更新坐席分组成功", agentGroupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("group:manage")
    public R<Void> delete(@PathVariable Long id) {
        agentGroupService.delete(id);
        return R.success("删除坐席分组成功");
    }
}
