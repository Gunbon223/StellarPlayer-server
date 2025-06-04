package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.Voucher;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.OrderRequest;
import org.gb.stellarplayer.Request.SubscriptionRequest;
import org.gb.stellarplayer.Response.OrderResponse;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Service.SubscriptionService;
import org.gb.stellarplayer.Service.VoucherService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionApi {
    private final SubscriptionService subscriptionService;
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getSubscription();
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable int id) {
        Subscription subscription = subscriptionService.getSubscriptionById(id);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<OrderResponse> purchaseSubscription(
            @PathVariable int id,
            @RequestParam(required = false) String voucherCode,
            @RequestParam int userId,
            @RequestParam String baseUrl,
            @RequestHeader("Authorization") String token) {

        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);

        // Get username from token
        String username = jwtUtil.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Verify user has permission to make this purchase
        if (user.getId() != userId && !hasAdminRole(user)) {
            throw new BadRequestException("You don't have permission to make this purchase");
        }

        // Create order
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setSubscriptionId(id);
        orderRequest.setVoucherCode(voucherCode);

        // Process order
        try {
            org.gb.stellarplayer.Entites.Order order = orderService.createOrder(
                    orderRequest.getUserId(),
                    orderRequest.getSubscriptionId(),
                    orderRequest.getVoucherCode()
            );

            // Convert to response
            OrderResponse response = convertToOrderResponse(order);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new BadRequestException("Failed to create order: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/calculate-price")
    public ResponseEntity<Map<String, Object>> calculatePrice(
            @PathVariable int id,
            @RequestParam(required = false) String voucherCode,
            @RequestHeader("Authorization") String token) {

        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);

        Map<String, Object> response = new HashMap<>();

        try {
            // Get subscription
            Subscription subscription = subscriptionService.getSubscriptionById(id);
            double originalPrice = subscription.getPrice();
            response.put("originalPrice", originalPrice);

            // Apply voucher if provided
            if (voucherCode != null && !voucherCode.isEmpty()) {
                if (voucherService.isVoucherValid(voucherCode)) {
                    Voucher voucher = voucherService.getVoucherByCode(voucherCode);
                    double discountAmount = originalPrice * (voucher.getDiscountPercentage() / 100.0);
                    double finalPrice = originalPrice - discountAmount;

                    // Ensure final price is not negative
                    if (finalPrice < 0) {
                        finalPrice = 0.0;
                    }

                    response.put("discountPercentage", voucher.getDiscountPercentage());
                    response.put("discountAmount", discountAmount);
                    response.put("finalPrice", finalPrice);
                    response.put("voucherValid", true);
                } else {
                    response.put("voucherValid", false);
                    response.put("finalPrice", originalPrice);
                }
            } else {
                response.put("finalPrice", originalPrice);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateAdminToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                if (!hasAdminRole(user)) {
                    throw new BadRequestException("Access denied. Admin privileges required");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }

    private OrderResponse convertToOrderResponse(org.gb.stellarplayer.Entites.Order order) {
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
