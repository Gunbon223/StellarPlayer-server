package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.UserAdminService;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin-only controller for user management and analytics
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserMngApi {
    private final UserAdminService userAdminService;
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
        response.put("total_users", userAdminService.getTotalUsersCount());
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
        response.put("new_users", userAdminService.getNewUsersCountByPeriod(period));
        
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
     * Get weekly user analytics for current month
     * Shows new users for each week of the current month with subscription breakdown
     * @param token Admin authentication token
     * @return Weekly user analytics
     */
    @GetMapping("/analytics/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyUserAnalytics(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = userAdminService.getWeeklyUserAnalytics();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get monthly user analytics for current year
     * Shows new users for each month of the current year with subscription breakdown
     * @param year Optional year parameter (defaults to current year)
     * @param token Admin authentication token
     * @return Monthly user analytics
     */
    @GetMapping("/analytics/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyUserAnalytics(
            @RequestParam(required = false) Integer year,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = userAdminService.getMonthlyUserAnalytics(year);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get yearly user analytics
     * Shows new users for current year and next 3-4 years with subscription breakdown
     * @param years Number of future years to include (default: 4, max: 5)
     * @param token Admin authentication token
     * @return Yearly user analytics
     */
    @GetMapping("/analytics/yearly")
    public ResponseEntity<Map<String, Object>> getYearlyUserAnalytics(
            @RequestParam(defaultValue = "4") int years,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        // Limit years to prevent excessive projection
        if (years > 5) years = 5;
        if (years < 1) years = 4;
        
        Map<String, Object> response = userAdminService.getYearlyUserAnalytics(years);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get comprehensive user dashboard analytics
     * Includes weekly, monthly, and subscription breakdowns
     * @param token Admin authentication token
     * @return Comprehensive dashboard analytics
     */
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<Map<String, Object>> getComprehensiveUserAnalytics(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = userAdminService.getComprehensiveUserAnalytics();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user subscription status breakdown
     * @param token Admin authentication token
     * @return User subscription status breakdown
     */
    @GetMapping("/analytics/subscription-breakdown")
    public ResponseEntity<Map<String, Object>> getUserSubscriptionBreakdown(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);

        Map<String, Object> response = userAdminService.getUserSubscriptionBreakdown();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user growth trends and projections
     * @param token Admin authentication token
     * @return User growth trends and projections
     */
    @GetMapping("/analytics/growth-trends")
    public ResponseEntity<Map<String, Object>> getUserGrowthTrends(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);

        Map<String, Object> response = userAdminService.getUserGrowthTrends();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get comprehensive dashboard statistics (enhanced version)
     * @param token Admin authentication token
     * @return Enhanced dashboard statistics
     */
    @GetMapping("/stats/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        
        Map<String, Object> response = new HashMap<>();
        
        // Total counts
        response.put("total_users", userAdminService.getTotalUsersCount());
        response.put("total_subscribed_users", userSubscriptionService.getTotalActiveSubscriptionsCount());
        
        // Monthly new users and subscriptions
        response.put("monthly_users", userAdminService.getNewUsersCountByPeriod("month"));
        response.put("monthly_subscriptions", userSubscriptionService.getNewSubscriptionsCountByPeriod("month"));
        
        // Quarterly new users and subscriptions
        response.put("quarterly_users", userAdminService.getNewUsersCountByPeriod("quarter"));
        response.put("quarterly_subscriptions", userSubscriptionService.getNewSubscriptionsCountByPeriod("quarter"));
        
        // Yearly new users and subscriptions
        response.put("yearly_users", userAdminService.getNewUsersCountByPeriod("year"));
        response.put("yearly_subscriptions", userSubscriptionService.getNewSubscriptionsCountByPeriod("year"));
        
        // Add subscription breakdown
        Map<String, Object> subscriptionBreakdown = userAdminService.getUserSubscriptionBreakdown();
        response.put("subscription_breakdown", subscriptionBreakdown);
        
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
