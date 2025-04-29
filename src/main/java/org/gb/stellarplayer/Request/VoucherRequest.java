package org.gb.stellarplayer.Request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    private String code;
    private int maxUseCount;
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
