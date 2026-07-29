package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FaqSemanticConfigRequest {
    @NotNull private Integer enabled;
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") private BigDecimal similarityThreshold;
    @NotNull @Min(5) @Max(100) private Integer maxCandidates;
    @NotNull @Min(1) @Max(20) private Integer maxResults;
}
