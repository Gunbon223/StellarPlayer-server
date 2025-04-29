package org.gb.stellarplayer.Service;

import java.util.Map;

/**
 * Service for handling payment processing
 */
public interface PaymentService {
    
    /**
     * Process a payment return from VNPAY
     * 
     * @param vnpParams Map of VNPAY parameters
     * @return Map containing response data including status, message, and redirect URL
     */
    Map<String, Object> processVnpayReturn(Map<String, String> vnpParams);
    
    /**
     * Extract order code from VNPAY order info
     * 
     * @param orderInfo Order info string from VNPAY
     * @return Extracted order code
     */
    String extractOrderCodeFromOrderInfo(String orderInfo);
    
    /**
     * Log a payment transaction for audit purposes
     * 
     * @param orderCode Order code
     * @param responseCode Response code from payment gateway
     * @param transactionId Transaction ID from payment gateway
     * @param newStatus New status of the order
     */
    void logPaymentTransaction(String orderCode, String responseCode, String transactionId, String newStatus);
    
    /**
     * Check for pending orders and update their status if they've been pending for too long
     * 
     * @return Map containing information about the updated orders
     */
    Map<String, Object> checkAndUpdatePendingOrders();
}
