package org.gb.stellarplayer.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherResponse {
    private int id;
    private String code;
    private int usedCount;
    private int maxUseCount;
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}
