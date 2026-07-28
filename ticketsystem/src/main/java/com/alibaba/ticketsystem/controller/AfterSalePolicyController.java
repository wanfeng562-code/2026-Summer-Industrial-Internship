package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.AfterSalePolicyRequest;
import com.alibaba.ticketsystem.dto.PolicyEnabledRequest;
import com.alibaba.ticketsystem.service.AfterSalePolicyService;
import com.alibaba.ticketsystem.utils.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/policies")
@SaCheckPermission("policy:manage")
public class AfterSalePolicyController {

    private final AfterSalePolicyService policyService;

    @GetMapping
    public R<?> page(
            @RequestParam(defaultValue = "1") @Min(1) Integer current,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer enabled) {
        return R.success("售后策略分页查询成功",
                policyService.page(current, size, category, enabled));
    }

    @GetMapping("/{id}")
    public R<?> get(@PathVariable Long id) {
        return R.success("售后策略查询成功", policyService.get(id));
    }

    @PostMapping
    public R<?> create(@Valid @RequestBody AfterSalePolicyRequest request) {
        return R.success("售后策略创建成功", policyService.create(request));
    }

    @PutMapping("/{id}")
    public R<?> update(@PathVariable Long id, @Valid @RequestBody AfterSalePolicyRequest request) {
        return R.success("售后策略修改成功", policyService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public R<?> setEnabled(@PathVariable Long id, @Valid @RequestBody PolicyEnabledRequest request) {
        policyService.setEnabled(id, request.getEnabled());
        return R.success("售后策略启用状态修改成功");
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        policyService.delete(id);
        return R.success("售后策略删除成功");
    }
}
