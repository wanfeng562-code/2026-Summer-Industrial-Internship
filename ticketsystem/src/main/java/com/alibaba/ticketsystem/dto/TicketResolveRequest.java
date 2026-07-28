package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketResolveRequest {

    @NotBlank(message = "解决说明不能为空")
    @Size(max = 2000, message = "解决说明不能超过2000个字符")
    private String content;
}
