package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AfterSalePolicyRequest {

    @NotBlank(message = "策略名称不能为空")
    @Size(max = 100, message = "策略名称不能超过100个字符")
    private String policyName;

    @NotBlank(message = "适用分类不能为空")
    @Pattern(regexp = "REFUND|LOGISTICS|DAMAGE|INVOICE|OTHER", message = "适用分类不正确")
    private String category;

    @NotBlank(message = "条件类型不能为空")
    @Pattern(regexp = "ALWAYS|AMOUNT_REPUTATION|AMOUNT|REPUTATION", message = "条件类型不正确")
    private String conditionType;

    @DecimalMin(value = "0", message = "最小金额不能小于0")
    private BigDecimal minAmount;

    @DecimalMin(value = "0", message = "最大金额不能小于0")
    private BigDecimal maxAmount;

    @Min(value = 0, message = "最低信誉分不能小于0")
    @Max(value = 100, message = "最低信誉分不能超过100")
    private Integer minReputation;

    @NotBlank(message = "处理动作不能为空")
    @Pattern(regexp = "AUTO_APPROVE|AUTO_REPLY|MANUAL", message = "处理动作不正确")
    private String action;

    @Size(max = 5000, message = "回复模板不能超过5000个字符")
    private String replyTemplate;

    @NotNull(message = "策略优先级不能为空")
    @Min(value = 0, message = "策略优先级不能小于0")
    private Integer priority;

    @NotNull(message = "启用状态不能为空")
    @Min(value = 0, message = "启用状态只能是0或1")
    @Max(value = 1, message = "启用状态只能是0或1")
    private Integer enabled;

    @Min(value = 1, message = "SLA时长必须大于0")
    @Max(value = 720, message = "SLA时长不能超过720小时")
    private Integer slaHours;
}
