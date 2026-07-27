package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVo {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String logisticsStatus;
    private String logisticsNo;
    private LocalDateTime orderTime;
    private LocalDateTime payTime;
    private LocalDateTime deliverTime;
    private LocalDateTime receiveTime;

}
