package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.TicketCategoryRequest;
import com.alibaba.ticketsystem.entity.TicketCategory;
import com.alibaba.ticketsystem.service.TicketCategoryService;
import com.alibaba.ticketsystem.service.UserService;
import com.alibaba.ticketsystem.utils.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 工单分类及默认负责组配置。 */
@RestController
@RequestMapping("/ticket-categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;
    private final UserService userService;

    @GetMapping
    @SaCheckPermission("ticket:query")
    public R<List<TicketCategory>> list(@RequestParam(defaultValue = "false") boolean includeDisabled) {
        boolean canSeeDisabled = includeDisabled && "ADMIN".equals(userService.requireCurrentUser().getRole());
        return R.success("查询工单分类成功", ticketCategoryService.list(canSeeDisabled));
    }

    @PostMapping
    @SaCheckPermission("category:manage")
    public R<TicketCategory> create(@Valid @RequestBody TicketCategoryRequest request) {
        return R.success("创建工单分类成功", ticketCategoryService.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("category:manage")
    public R<TicketCategory> update(@PathVariable Long id, @Valid @RequestBody TicketCategoryRequest request) {
        return R.success("更新工单分类成功", ticketCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("category:manage")
    public R<Void> delete(@PathVariable Long id) {
        ticketCategoryService.delete(id);
        return R.success("删除工单分类成功");
    }
}
