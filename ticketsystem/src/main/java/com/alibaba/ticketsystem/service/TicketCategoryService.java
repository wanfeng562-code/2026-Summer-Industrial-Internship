package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.TicketCategoryRequest;
import com.alibaba.ticketsystem.entity.AfterSalePolicy;
import com.alibaba.ticketsystem.entity.Faq;
import com.alibaba.ticketsystem.entity.Ticket;
import com.alibaba.ticketsystem.entity.TicketCategory;
import com.alibaba.ticketsystem.mapper.TicketCategoryMapper;
import com.alibaba.ticketsystem.mapper.AfterSalePolicyMapper;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryMapper categoryMapper;
    private final AgentGroupService groupService;
    private final TicketMapper ticketMapper;
    private final FaqMapper faqMapper;
    private final AfterSalePolicyMapper policyMapper;

    public List<TicketCategory> list(boolean includeDisabled) {
        QueryWrapper<TicketCategory> query = new QueryWrapper<TicketCategory>()
                .eq("deleted", 0).orderByAsc("id");
        if (!includeDisabled) {
            query.eq("enabled", 1);
        }
        return categoryMapper.selectList(query);
    }

    public TicketCategory requireActive(String code) {
        TicketCategory category = categoryMapper.selectOne(new QueryWrapper<TicketCategory>()
                .eq("category_code", normalizeCode(code)).eq("deleted", 0).eq("enabled", 1));
        if (category == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "工单分类不存在或已禁用");
        }
        return category;
    }

    public String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    @Transactional
    public TicketCategory create(TicketCategoryRequest request) {
        String code = normalizeCode(request.getCategoryCode());
        if (categoryMapper.selectCount(new QueryWrapper<TicketCategory>()
                .eq("category_code", code).eq("deleted", 0)) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "分类编码已存在");
        }
        TicketCategory category = new TicketCategory();
        apply(category, request);
        category.setDeleted(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public TicketCategory update(Long id, TicketCategoryRequest request) {
        TicketCategory category = require(id);
        String code = normalizeCode(request.getCategoryCode());
        if (!code.equals(category.getCategoryCode())) {
            throw new ApiException(HttpStatus.CONFLICT, "分类编码创建后不可修改，请新增分类并迁移业务数据");
        }
        if (categoryMapper.selectCount(new QueryWrapper<TicketCategory>()
                .eq("category_code", code).eq("deleted", 0).ne("id", id)) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "分类编码已存在");
        }
        apply(category, request);
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
        return category;
    }

    @Transactional
    public void delete(Long id) {
        TicketCategory category = require(id);
        String code = category.getCategoryCode();
        if (ticketMapper.selectCount(new QueryWrapper<Ticket>()
                .eq("category", code).eq("deleted", 0)) > 0
                || faqMapper.selectCount(new QueryWrapper<Faq>()
                .eq("category", code).eq("deleted", 0)) > 0
                || policyMapper.selectCount(new QueryWrapper<AfterSalePolicy>()
                .eq("category", code).eq("deleted", 0)) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "该分类已有工单、FAQ或策略引用，不能删除，可改为停用");
        }
        category.setDeleted(1);
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
    }

    public TicketCategory require(Long id) {
        TicketCategory category = categoryMapper.selectById(id);
        if (category == null || Integer.valueOf(1).equals(category.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "工单分类不存在");
        }
        return category;
    }

    private void apply(TicketCategory category, TicketCategoryRequest request) {
        if (request.getGroupId() != null) {
            if (Integer.valueOf(1).equals(request.getEnabled())) {
                groupService.requireActive(request.getGroupId());
            } else {
                groupService.require(request.getGroupId());
            }
        }
        category.setCategoryCode(normalizeCode(request.getCategoryCode()));
        category.setCategoryName(request.getCategoryName().trim());
        category.setGroupId(request.getGroupId());
        category.setEnabled(request.getEnabled());
    }
}
