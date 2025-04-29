package org.gb.stellarplayer.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store payment transaction logs
 * This helps with auditing and tracking payment transactions
 */
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_transaction")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    private Order order;
    
    private String responseCode;
    private String transactionId;
    private String newStatus;
    private String requestData; // Store the raw request data for debugging
    private LocalDateTime transactionTime;
    
    // Additional fields for tracking
    private String ipAddress;
    private String userAgent;
}
