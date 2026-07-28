package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.AfterSalePolicyRequest;
import com.alibaba.ticketsystem.entity.AfterSalePolicy;
import com.alibaba.ticketsystem.mapper.AfterSalePolicyMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AfterSalePolicyServiceTest {

    @Mock
    private AfterSalePolicyMapper policyMapper;

    @Test
    @SuppressWarnings("unchecked")
    void firstMatchingPolicyControlsSla() {
        AfterSalePolicy smallRefund = policy("AMOUNT_REPUTATION",
                new BigDecimal("0"), new BigDecimal("50"), 80, 12);
        AfterSalePolicy fallback = policy("ALWAYS", null, null, null, 48);
        when(policyMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(smallRefund, fallback));
        AfterSalePolicyService service = new AfterSalePolicyService(policyMapper);

        assertThat(service.resolveSlaHours(
                "REFUND", new BigDecimal("30"), 90, "MEDIUM")).isEqualTo(12);
    }

    @Test
    @SuppressWarnings("unchecked")
    void nonMatchingRuleFallsThroughToNextPolicy() {
        AfterSalePolicy highReputation = policy("REPUTATION", null, null, 90, 8);
        AfterSalePolicy fallback = policy("ALWAYS", null, null, null, 36);
        when(policyMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(highReputation, fallback));
        AfterSalePolicyService service = new AfterSalePolicyService(policyMapper);

        assertThat(service.resolveSlaHours(
                "LOGISTICS", new BigDecimal("100"), 70, "HIGH")).isEqualTo(36);
    }

    @Test
    @SuppressWarnings("unchecked")
    void priorityDefaultIsUsedWithoutMatchedPolicy() {
        when(policyMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());
        AfterSalePolicyService service = new AfterSalePolicyService(policyMapper);

        assertThat(service.resolveSlaHours(
                "OTHER", new BigDecimal("100"), 80, "URGENT")).isEqualTo(4);
    }

    @Test
    void invalidAmountRangeReturns400() {
        AfterSalePolicyService service = new AfterSalePolicyService(policyMapper);
        AfterSalePolicyRequest request = validRequest();
        request.setMinAmount(new BigDecimal("100"));
        request.setMaxAmount(new BigDecimal("10"));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private AfterSalePolicy policy(String condition,
                                   BigDecimal min,
                                   BigDecimal max,
                                   Integer reputation,
                                   Integer slaHours) {
        AfterSalePolicy policy = new AfterSalePolicy();
        policy.setConditionType(condition);
        policy.setMinAmount(min);
        policy.setMaxAmount(max);
        policy.setMinReputation(reputation);
        policy.setSlaHours(slaHours);
        return policy;
    }

    private AfterSalePolicyRequest validRequest() {
        AfterSalePolicyRequest request = new AfterSalePolicyRequest();
        request.setPolicyName("测试策略");
        request.setCategory("REFUND");
        request.setConditionType("AMOUNT");
        request.setMinAmount(BigDecimal.ZERO);
        request.setMaxAmount(new BigDecimal("100"));
        request.setAction("MANUAL");
        request.setPriority(1);
        request.setEnabled(1);
        return request;
    }
}
