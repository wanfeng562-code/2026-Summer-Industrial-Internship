package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.domain.TicketPriority;
import com.alibaba.ticketsystem.dto.AfterSalePolicyRequest;
import com.alibaba.ticketsystem.entity.AfterSalePolicy;
import com.alibaba.ticketsystem.mapper.AfterSalePolicyMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AfterSalePolicyService {

    private static final List<String> CATEGORIES =
            List.of("REFUND", "LOGISTICS", "DAMAGE", "INVOICE", "OTHER");

    private final AfterSalePolicyMapper policyMapper;

    public Page<AfterSalePolicy> page(int current, int size, String category, Integer enabled) {
        QueryWrapper<AfterSalePolicy> query = new QueryWrapper<AfterSalePolicy>()
                .eq("deleted", 0)
                .orderByAsc("priority")
                .orderByDesc("id");
        if (StringUtils.hasText(category)) {
            query.eq("category", normalizeCategory(category));
        }
        if (enabled != null) {
            if (enabled != 0 && enabled != 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "启用状态只能是0或1");
            }
            query.eq("enabled", enabled);
        }
        return policyMapper.selectPage(new Page<>(current, size), query);
    }

    public AfterSalePolicy get(Long id) {
        AfterSalePolicy policy = policyMapper.selectById(id);
        if (policy == null || Integer.valueOf(1).equals(policy.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "售后策略不存在");
        }
        return policy;
    }

    @Transactional
    public AfterSalePolicy create(AfterSalePolicyRequest request) {
        validateRule(request);
        AfterSalePolicy policy = new AfterSalePolicy();
        apply(policy, request);
        policy.setDeleted(0);
        policy.setCreateTime(LocalDateTime.now());
        policy.setUpdateTime(LocalDateTime.now());
        policyMapper.insert(policy);
        return policy;
    }

    @Transactional
    public AfterSalePolicy update(Long id, AfterSalePolicyRequest request) {
        validateRule(request);
        AfterSalePolicy policy = get(id);
        apply(policy, request);
        policy.setUpdateTime(LocalDateTime.now());
        policyMapper.updateById(policy);
        return policy;
    }

    @Transactional
    public void setEnabled(Long id, Integer enabled) {
        AfterSalePolicy policy = get(id);
        policy.setEnabled(enabled);
        policy.setUpdateTime(LocalDateTime.now());
        policyMapper.updateById(policy);
    }

    @Transactional
    public void delete(Long id) {
        AfterSalePolicy policy = get(id);
        policy.setDeleted(1);
        policy.setUpdateTime(LocalDateTime.now());
        policyMapper.updateById(policy);
    }

    public Optional<AfterSalePolicy> match(String category, BigDecimal amount, Integer reputationScore) {
        List<AfterSalePolicy> candidates = policyMapper.selectList(
                new QueryWrapper<AfterSalePolicy>()
                        .eq("category", normalizeCategory(category))
                        .eq("enabled", 1)
                        .eq("deleted", 0)
                        .orderByAsc("priority")
                        .orderByAsc("id"));
        return candidates.stream()
                .filter(policy -> matches(policy, amount, reputationScore))
                .findFirst();
    }

    public int resolveSlaHours(String category,
                               BigDecimal amount,
                               Integer reputationScore,
                               String priority) {
        return match(category, amount, reputationScore)
                .map(AfterSalePolicy::getSlaHours)
                .filter(hours -> hours != null && hours > 0)
                .orElseGet(() -> TicketPriority.from(priority).getDefaultSlaHours());
    }

    private boolean matches(AfterSalePolicy policy, BigDecimal amount, Integer reputationScore) {
        boolean amountMatches = amount != null
                && (policy.getMinAmount() == null || amount.compareTo(policy.getMinAmount()) >= 0)
                && (policy.getMaxAmount() == null || amount.compareTo(policy.getMaxAmount()) <= 0);
        boolean reputationMatches = reputationScore != null
                && (policy.getMinReputation() == null || reputationScore >= policy.getMinReputation());
        return switch (policy.getConditionType()) {
            case "ALWAYS" -> true;
            case "AMOUNT" -> amountMatches;
            case "REPUTATION" -> reputationMatches;
            case "AMOUNT_REPUTATION" -> amountMatches && reputationMatches;
            default -> false;
        };
    }

    private void validateRule(AfterSalePolicyRequest request) {
        if (request.getMinAmount() != null && request.getMaxAmount() != null
                && request.getMinAmount().compareTo(request.getMaxAmount()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "最小金额不能大于最大金额");
        }
        if (request.getConditionType().contains("AMOUNT")
                && request.getMinAmount() == null && request.getMaxAmount() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "金额条件至少需要填写一个金额边界");
        }
        if (request.getConditionType().contains("REPUTATION") && request.getMinReputation() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "信誉条件必须填写最低信誉分");
        }
    }

    private String normalizeCategory(String category) {
        String value = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "适用分类不正确");
        }
        return value;
    }

    private void apply(AfterSalePolicy policy, AfterSalePolicyRequest request) {
        policy.setPolicyName(request.getPolicyName().trim());
        policy.setCategory(normalizeCategory(request.getCategory()));
        policy.setConditionType(request.getConditionType());
        policy.setMinAmount(request.getMinAmount());
        policy.setMaxAmount(request.getMaxAmount());
        policy.setMinReputation(request.getMinReputation());
        policy.setAction(request.getAction());
        policy.setReplyTemplate(StringUtils.hasText(request.getReplyTemplate())
                ? request.getReplyTemplate().trim() : null);
        policy.setPriority(request.getPriority());
        policy.setEnabled(request.getEnabled());
        policy.setSlaHours(request.getSlaHours());
    }
}
