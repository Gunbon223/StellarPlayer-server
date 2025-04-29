package org.gb.stellarplayer.Request;

import lombok.Data;

@Data
public class OrderRequest {
    private int userId;
    private int subscriptionId;
    private String voucherCode;
}
