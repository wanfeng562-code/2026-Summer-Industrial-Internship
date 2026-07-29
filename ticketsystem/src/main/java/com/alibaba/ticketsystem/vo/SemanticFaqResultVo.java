package com.alibaba.ticketsystem.vo;

import com.alibaba.ticketsystem.entity.Faq;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SemanticFaqResultVo {
    private Faq faq;
    private BigDecimal similarity;
}
