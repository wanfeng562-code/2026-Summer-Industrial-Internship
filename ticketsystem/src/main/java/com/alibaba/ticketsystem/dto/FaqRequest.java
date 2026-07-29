package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FaqRequest {

    @NotBlank(message = "FAQ分类不能为空")
    @Size(max = 30, message = "FAQ分类不能超过30个字符")
    private String category;

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题不能超过500个字符")
    private String question;

    @NotBlank(message = "答案不能为空")
    @Size(max = 5000, message = "答案不能超过5000个字符")
    private String answer;

    @Size(max = 500, message = "关键词不能超过500个字符")
    private String keywords;

    @NotNull(message = "启用状态不能为空")
    @Min(value = 0, message = "启用状态只能是0或1")
    @Max(value = 1, message = "启用状态只能是0或1")
    private Integer enabled;
}
