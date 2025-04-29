package org.gb.stellarplayer.REST;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Response.OrderResponse;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Service.PaymentService;
import org.gb.stellarplayer.Service.VNPAYService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.gb.stellarplayer.Ultils.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for payment-related operations
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Log4j2
public class PaymentApi {
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final VNPAYService vnpayService;
    private final JwtUtil jwtUtil;
    
    /**
     * Generate a payment URL for an order
     * 
     * @param id The order ID
     * @param baseUrl The base URL for the return URL
     * @param token The JWT token
     * @return The payment URL
     */
    @PostMapping("/orders/{id}/payment-url")
    public ResponseEntity<OrderResponse> generatePaymentUrl(@PathVariable int id,
                                                          @RequestParam String baseUrl,
                                                          @RequestHeader("Authorization") String token) {
        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        
        // Get order
        Order order = orderService.getOrderById(id);
        
        // Generate payment URL
        String paymentUrl = orderService.generatePaymentUrl(order, baseUrl);
        
        // Convert to response object
        OrderResponse response = convertToOrderResponse(order);
//        response.setPaymentUrl(paymentUrl);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Handle VNPAY payment return
     * This endpoint processes the payment result from VNPAY and updates the order status
     */
    @GetMapping("/vnpay-payment-return")
    public ResponseEntity<Map<String, Object>> handleVnpayPaymentReturn(HttpServletRequest request) {
        // Check if this is a direct access (not from VNPAY)
        if (request.getParameter("vnp_ResponseCode") == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid request - missing required parameters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        // Extract all parameters from the request
        Map<String, String> vnpParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            vnpParams.put(paramName, paramValue);
        }
        
        // Process the payment return
        Map<String, Object> result = paymentService.processVnpayReturn(vnpParams);
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    /**
     * Check pending orders and update their status if needed
     * This endpoint is called periodically by a scheduled task
     */
    @GetMapping("/check-pending-orders")
    public ResponseEntity<Map<String, Object>> checkPendingOrders() {
        Map<String, Object> result = paymentService.checkAndUpdatePendingOrders();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    /**
     * Helper method to convert Order to OrderResponse
     */
    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
        response.setUserId(order.getUser().getId());
        response.setSubscriptionId(order.getSubscription().getId());
        response.setSubscriptionName(order.getSubscription().getName());
        response.setOriginalAmount(order.getOriginalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setPaidAt(order.getPaidAt());

        
        if (order.getAppliedVoucher() != null) {
            response.setVoucherCode(order.getAppliedVoucher().getCode());
            response.setVoucherDiscount(order.getAppliedVoucher().getDiscountPercentage());
        }
        
        return response;
    }
}
