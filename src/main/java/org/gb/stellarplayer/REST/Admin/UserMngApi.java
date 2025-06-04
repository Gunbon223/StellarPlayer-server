package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.UserService;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin-only controller for user management
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserMngApi {
    private final UserService userService;
    private final UserSubscriptionService userSubscriptionService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Get total users count
     * @param token Admin authentication token
     * @return Total users count
     */
    @GetMapping("/stats/total")
    public ResponseEntity<Map<String, Object>> getTotalUsers(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("total_users", userService.getTotalUsersCount());
        response.put("total_subscribed_users", userSubscriptionService.getTotalActiveSubscriptionsCount());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get new users by period (month, quarter, year)
     * @param period The period (month, quarter, year)
     * @param token Admin authentication token
     * @return New users count for the specified period
     */
    @GetMapping("/stats/new")
    public ResponseEntity<Map<String, Object>> getNewUsers(
            @RequestParam(defaultValue = "month") String period,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("period", period);
        response.put("new_users", userService.getNewUsersCountByPeriod(period));
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get new subscriptions by period (month, quarter, year)
     * @param period The period (month, quarter, year)
     * @param token Admin authentication token
     * @return New subscriptions count for the specified period
     */
    @GetMapping("/stats/subscriptions")
    public ResponseEntity<Map<String, Object>> getNewSubscriptions(
            @RequestParam(defaultValue = "month") String period,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("period", period);
        response.put("new_subscriptions", userSubscriptionService.getNewSubscriptionsCountByPeriod(period));
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get comprehensive dashboard statistics
     * @param token Admin authentication token
     * @return Dashboard statistics
     */
    @GetMapping("/stats/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = new HashMap<>();
        
        // Total counts
        response.put("total_users", userService.getTotalUsersCount());
        response.put("total_subscribed_users", userSubscriptionService.getTotalActiveSubscriptionsCount());
        
        // Monthly new users and subscriptions
        response.put("monthly_users", userService.getNewUsersCountByPeriod("month"));
        response.put("monthly_subscriptions", userSubscriptionService.getNewSubscriptionsCountByPeriod("month"));
        
        // Quarterly new users and subscriptions
        response.put("quarterly_users", userService.getNewUsersCountByPeriod("quarter"));
        response.put("quarterly_subscriptions", userSubscriptionService.getNewSubscriptionsCountByPeriod("quarter"));
        
        // Yearly new users and subscriptions
        response.put("yearly_users", userService.getNewUsersCountByPeriod("year"));
        response.put("yearly_subscriptions", userSubscriptionService.getNewSubscriptionsCountByPeriod("year"));
        
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
