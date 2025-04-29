package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.PaymentTransaction;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.OrderRepository;
import org.gb.stellarplayer.Repository.PaymentTransactionRepository;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Service.PaymentService;
import org.gb.stellarplayer.Service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the PaymentService interface
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    @Transactional
    public Map<String, Object> processVnpayReturn(Map<String, String> vnpParams) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extract parameters from VNPAY response
            String orderInfo = vnpParams.get("vnp_OrderInfo");
            String responseCode = vnpParams.get("vnp_ResponseCode");
            String transactionId = vnpParams.get("vnp_TransactionNo");
            String paymentDate = vnpParams.get("vnp_PayDate");
            String amount = vnpParams.get("vnp_Amount");

            // Extract order code from order info
            String orderCode = extractOrderCodeFromOrderInfo(orderInfo);
            if (orderCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid order information");
                return response;
            }

            // Get the order
            Order order;
            try {
                order = orderService.getOrderByCode(orderCode);
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Order not found: " + e.getMessage());
                return response;
            }

            // Check if the order has already been processed
            String currentStatus = order.getStatus();
            if (isOrderInFinalState(currentStatus)) {
                response.put("alreadyProcessed", true);
                response.put("message", "Order has already been processed. Current status: " + currentStatus);

                // Return the current order information without updating
                response.put("orderCode", orderCode);
                response.put("status", currentStatus);
                response.put("transactionId", order.getTransactionId());

                // Include user and subscription details for redirection
                response.put("userId", order.getUser().getId());
                response.put("subscriptionId", order.getSubscription().getId());
                response.put("subscriptionName", order.getSubscription().getName());

                // Set redirect URL based on current status
                if ("PAID".equals(currentStatus)) {
                    String redirectUrl = String.format("http://localhost:3000/userdetail/%d", order.getUser().getId());
                    response.put("redirectUrl", redirectUrl);
                } else {
                    response.put("redirectUrl", "http://localhost:3000/payment-failed");
                }

                return response;
            }

            // Process based on response code
            Map<String, String> statusInfo = determineOrderStatus(responseCode);
            String newStatus = statusInfo.get("status");
            String message = statusInfo.get("message");

            // Update order status
            orderService.updateOrderStatus(orderCode, newStatus, transactionId);

            // Log the transaction for audit purposes
            logPaymentTransaction(orderCode, responseCode, transactionId, newStatus);

            // Prepare response
            response.put("success", true);
            response.put("message", message);
            response.put("orderCode", orderCode);
            response.put("status", newStatus);
            response.put("transactionId", transactionId);
            response.put("amount", amount != null ? Double.parseDouble(amount) / 100 : 0); // Convert from VND cents
            response.put("paymentDate", paymentDate);

            // If payment was successful, include subscription details
            if ("PAID".equals(newStatus)) {
                response.put("userId", order.getUser().getId());
                response.put("subscriptionId", order.getSubscription().getId());
                response.put("subscriptionName", order.getSubscription().getName());

                // Include redirect URL for frontend
                String redirectUrl = String.format("http://localhost:3000/userdetail/%d", order.getUser().getId());
                response.put("redirectUrl", redirectUrl);
            } else if ("CANCELLED".equals(newStatus)) {
                // Include redirect URL for payment failed page
                response.put("redirectUrl", "http://localhost:3000/payment-failed");
            }

            return response;
        } catch (Exception e) {
            log.error("Error processing VNPAY return: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error processing payment: " + e.getMessage());
            return response;
        }
    }

    @Override
    public String extractOrderCodeFromOrderInfo(String orderInfo) {
        if (orderInfo != null && orderInfo.contains("Order: ")) {
            return orderInfo.substring(orderInfo.lastIndexOf("Order: ") + 7).trim();
        }
        return "";
    }

    @Override
    public void logPaymentTransaction(String orderCode, String responseCode, String transactionId, String newStatus) {
        try {
            // Create a log entry with timestamp
            String logEntry = String.format(
                "[%s] Payment transaction - Order: %s, Response Code: %s, Transaction ID: %s, New Status: %s",
                LocalDateTime.now().toString(),
                orderCode,
                responseCode,
                transactionId,
                newStatus
            );

            // Log to console
            log.info(logEntry);

            // Get the order
            Order order = orderService.getOrderByCode(orderCode);

            // Create and save a payment transaction record
            PaymentTransaction transaction = PaymentTransaction.builder()
                .order(order)
                .responseCode(responseCode)
                .transactionId(transactionId)
                .newStatus(newStatus)
                .transactionTime(LocalDateTime.now())
                .build();

            paymentTransactionRepository.save(transaction);
        } catch (Exception e) {
            // Don't let logging errors affect the main process
            log.error("Error logging payment transaction: {}", e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> checkAndUpdatePendingOrders() {
        Map<String, Object> response = new HashMap<>();
        int updatedCount = 0;

        try {
            // Get all orders from the repository
            List<Order> allOrders = orderService.getAllOrders();

            // Filter for pending orders that are older than 15 minutes
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);

            for (Order order : allOrders) {
                // Check if the order is pending and created more than 15 minutes ago
                if ("PENDING".equals(order.getStatus()) &&
                    order.getCreatedAt().isBefore(fifteenMinutesAgo)) {

                    try {
                        // Process each order in its own transaction
                        updatePendingOrder(order);
                        updatedCount++;
                    } catch (Exception ex) {
                        log.error("Error updating pending order {}: {}", order.getOrderCode(), ex.getMessage(), ex);
                        // Continue with the next order
                    }
                }
            }

            response.put("success", true);
            response.put("message", "Updated " + updatedCount + " pending orders to cancelled");
            response.put("updatedCount", updatedCount);

            return response;
        } catch (Exception e) {
            log.error("Error updating pending orders: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error updating pending orders: " + e.getMessage());
            return response;
        }
    }

    /**
     * Update a pending order to cancelled status
     * Each order is processed in its own transaction
     *
     * @param order The pending order
     */
    @Transactional
    public void updatePendingOrder(Order order) {
        // Update the order status to CANCELLED
        orderService.updateOrderStatus(order.getOrderCode(), "CANCELLED", null);

        // Log the transaction
        logPaymentTransaction(order.getOrderCode(), "TIMEOUT", null, "CANCELLED");

        log.info("Updated pending order {} to CANCELLED", order.getOrderCode());
    }

    /**
     * Check if an order is in a final state
     *
     * @param status The order status
     * @return true if the order is in a final state, false otherwise
     */
    private boolean isOrderInFinalState(String status) {
        return "PAID".equals(status) || "CANCELLED".equals(status) ||
               "REFUNDED".equals(status) || "FRAUD".equals(status);
    }

    /**
     * Determine the new order status based on the VNPAY response code
     *
     * @param responseCode The VNPAY response code
     * @return A map containing the new status and a message
     */
    public Map<String, String> determineOrderStatus(String responseCode) {
        Map<String, String> result = new HashMap<>();
        String newStatus;
        String message;

        switch (responseCode) {
            case "00":
                // Successful transaction
                newStatus = "PAID";
                message = "Payment successful";
                break;
            case "01":
                // Transaction not completed
                newStatus = "PENDING";
                message = "Payment not completed";
                break;
            case "04":
                // Reversed transaction
                newStatus = "PENDING";
                message = "Payment reversed - please contact support";
                break;
            case "05":
            case "06":
                // Refund processing
                newStatus = "REFUNDING";
                message = "Refund in progress";
                break;
            case "07":
                // Suspected fraud
                newStatus = "FRAUD";
                message = "Payment flagged as suspicious";
                break;
            default:
                // Other error codes
                newStatus = "CANCELLED";
                message = "Payment failed with code: " + responseCode;
        }

        result.put("status", newStatus);
        result.put("message", message);
        return result;
    }
}
