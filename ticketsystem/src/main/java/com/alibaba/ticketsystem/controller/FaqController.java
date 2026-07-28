package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.FaqRequest;
import com.alibaba.ticketsystem.service.FaqService;
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
@RequestMapping("/faqs")
public class FaqController {

    private final FaqService faqService;

    @GetMapping("/search")
    @SaCheckPermission("faq:query")
    public R<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return R.success("FAQ检索成功", faqService.search(keyword, category));
    }

    @GetMapping
    @SaCheckPermission("faq:manage")
    public R<?> page(
            @RequestParam(defaultValue = "1") @Min(1) Integer current,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(required = false) String keyword) {
        return R.success("FAQ分页查询成功",
                faqService.page(current, size, category, enabled, keyword));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("faq:manage")
    public R<?> get(@PathVariable Long id) {
        return R.success("FAQ查询成功", faqService.get(id));
    }

    @PostMapping
    @SaCheckPermission("faq:manage")
    public R<?> create(@Valid @RequestBody FaqRequest request) {
        return R.success("FAQ创建成功", faqService.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("faq:manage")
    public R<?> update(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return R.success("FAQ修改成功", faqService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("faq:manage")
    public R<?> delete(@PathVariable Long id) {
        faqService.delete(id);
        return R.success("FAQ删除成功");
    }
}
