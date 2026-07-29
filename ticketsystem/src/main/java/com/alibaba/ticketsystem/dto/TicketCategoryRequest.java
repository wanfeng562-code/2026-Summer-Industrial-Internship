package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCategoryRequest {
    @NotBlank(message = "分类编码不能为空")
    @Pattern(regexp = "[A-Z][A-Z0-9_]{1,29}", message = "分类编码必须为2至30位大写字母、数字或下划线")
    private String categoryCode;
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不能超过100个字符")
    private String categoryName;
    private Long groupId;
    @Min(0) @Max(1)
    private Integer enabled = 1;
}
