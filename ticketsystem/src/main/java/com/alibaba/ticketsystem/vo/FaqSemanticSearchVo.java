package com.alibaba.ticketsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class FaqSemanticSearchVo {
    private String mode;
    private List<SemanticFaqResultVo> results;
}
