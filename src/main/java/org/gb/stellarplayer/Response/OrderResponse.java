package org.gb.stellarplayer.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private int id;
    private String orderCode;
    private int userId;
    private int subscriptionId;
    private String subscriptionName;
    private Double originalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private String status;
    private String voucherCode;
    private Double voucherDiscount;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
