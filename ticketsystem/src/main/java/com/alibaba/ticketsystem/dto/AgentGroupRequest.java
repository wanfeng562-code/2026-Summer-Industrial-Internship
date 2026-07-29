package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AgentGroupRequest {
    @NotBlank(message = "坐席组名称不能为空")
    @Size(max = 100, message = "坐席组名称不能超过100个字符")
    private String groupName;
    private Long leaderId;
    @Size(max = 500, message = "说明不能超过500个字符")
    private String description;
    @Min(0) @Max(1)
    private Integer enabled = 1;
    @Size(max = 100, message = "坐席成员不能超过100人")
    private List<Long> agentIds;
}
