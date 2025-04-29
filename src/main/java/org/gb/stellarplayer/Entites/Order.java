package org.gb.stellarplayer.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderCode;
    private Double originalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private String status; // PENDING, PAID, CANCELLED
    private String paymentMethod;
    private String transactionId;

    @ManyToOne
    private User user;

    @ManyToOne
    private Subscription subscription;

    @ManyToOne
    private Voucher appliedVoucher;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
}
