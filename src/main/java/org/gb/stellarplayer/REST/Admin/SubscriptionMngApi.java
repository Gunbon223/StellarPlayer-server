package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.SubscriptionRequest;
import org.gb.stellarplayer.Service.RevenueStatisticsService;
import org.gb.stellarplayer.Service.SubscriptionService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only controller for subscription management
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
public class SubscriptionMngApi {
    private final SubscriptionService subscriptionService;
    private final RevenueStatisticsService revenueStatisticsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Get all subscriptions
     * @return List of all subscriptions
     */
    @GetMapping
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getSubscription();
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    /**
     * Get subscription by ID
     * @param id Subscription ID
     * @return Subscription details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable int id) {
        Subscription subscription = subscriptionService.getSubscriptionById(id);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    /**
     * Add a new subscription (admin only)
     * @param subscriptionRequest Subscription request data
     * @param token Admin authentication token
     * @return Created subscription
     */
    @PostMapping
    public ResponseEntity<?> addSubscription(
            @RequestBody SubscriptionRequest subscriptionRequest,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Subscription subscription = subscriptionService.addSubscription(subscriptionRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create subscription: " + e.getMessage()));
        }
    }

    /**
     * Update existing subscription (admin only)
     * @param subscriptionRequest Updated subscription data
     * @param id Subscription ID
     * @param token Admin authentication token
     * @return Updated subscription
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscription(
            @RequestBody SubscriptionRequest subscriptionRequest,
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Subscription subscription = subscriptionService.updateSubscription(subscriptionRequest, id);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update subscription: " + e.getMessage()));
        }
    }

    /**
     * Delete subscription (admin only)
     * @param id Subscription ID
     * @param token Admin authentication token
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscription(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            subscriptionService.deleteSubscription(id);
            return ResponseEntity.ok(Map.of("message", "Subscription deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete subscription: " + e.getMessage()));
        }
    }

    /**
     * Calculate revenue for a specific subscription by period
     * @param id Subscription ID
     * @param period The period (month, quarter, year)
     * @param token Admin authentication token
     * @return Revenue statistics
     */
    @GetMapping("/{id}/revenue")
    public ResponseEntity<Map<String, Object>> calculateRevenue(
            @PathVariable int id,
            @RequestParam(defaultValue = "month") String period,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        Map<String, Object> response = revenueStatisticsService.calculateSubscriptionRevenue(id, period);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Calculate revenue for all subscriptions
     * @param period The period (month, quarter, year)
     * @param token Admin authentication token
     * @return Revenue statistics for all subscriptions
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> calculateTotalRevenue(
            @RequestParam(defaultValue = "month") String period,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        Map<String, Object> response = revenueStatisticsService.calculateTotalRevenue(period);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Validate admin token
     * @param token JWT token
     * @throws BadRequestException If token is invalid or user is not an admin
     */
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

    /**
     * Check if user has admin role
     * @param user User entity
     * @return True if user has admin role, false otherwise
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }
}
