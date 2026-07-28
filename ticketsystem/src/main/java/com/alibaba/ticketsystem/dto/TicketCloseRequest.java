package com.alibaba.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCloseRequest {

    @NotBlank(message = "关闭说明不能为空")
    @Size(max = 500, message = "关闭说明不能超过500个字符")
    private String reason;
}
