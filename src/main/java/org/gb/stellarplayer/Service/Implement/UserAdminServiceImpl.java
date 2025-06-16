package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Role;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Model.Enum.EnumUserRole;
import org.gb.stellarplayer.Repository.RoleRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.UserAdminService;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {
    private final UserRepository userRepository;
    private final UserSubscriptionService userSubscriptionService;
    private final RoleRepository roleRepository;

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public Page<Map<String, Object>> getAllUsers(Pageable pageable, String search) {
        Page<User> users = userRepository.findUsersWithSearch(search, pageable);
        
        List<Map<String, Object>> usersList = users.getContent().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("enabled", user.getEnabled());
                    userMap.put("createdAt", user.getCreatedAt());
                    userMap.put("updatedAt", user.getUpdatedAt());
                    userMap.put("avatar", user.getAvatar());
                    userMap.put("dob", user.getDob());
                    
                    // Add subscription status
                    boolean isSubscribed = isUserSubscribed(user);
                    userMap.put("isSubscribed", isSubscribed);
                    
                    // Add role information
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    userMap.put("roles", roles);
                    
                    return userMap;
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(usersList, pageable, users.getTotalElements());
    }

    @Override
    public Page<Map<String, Object>> getSubscribedUsers(Pageable pageable, String search) {
        Page<User> subscribedUsers = userRepository.findSubscribedUsersWithSearch(search, pageable);
        
        List<Map<String, Object>> usersList = subscribedUsers.getContent().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("enabled", user.getEnabled());
                    userMap.put("createdAt", user.getCreatedAt());
                    userMap.put("updatedAt", user.getUpdatedAt());
                    userMap.put("avatar", user.getAvatar());
                    userMap.put("dob", user.getDob());
                    
                    // Add current subscription details
                    Map<String, Object> subscriptionDetails = getCurrentSubscriptionDetails(user.getId());
                    userMap.put("currentSubscription", subscriptionDetails);
                    
                    // Add longest subscription duration
                    Integer longestDuration = userRepository.findLongestSubscriptionDuration(user.getId());
                    userMap.put("longestSubscriptionDays", longestDuration != null ? longestDuration : 0);
                    
                    // Add role information
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    userMap.put("roles", roles);
                    
                    return userMap;
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(usersList, pageable, subscribedUsers.getTotalElements());
    }

    @Override
    public Map<String, Object> updateUserStatus(Integer userId, boolean disable, String reason) {
        boolean newStatus = !disable; // enabled = !disable
        int updatedRows = userRepository.updateUserEnabledStatus(userId, newStatus);
        
        if (updatedRows == 0) {
            throw new BadRequestException("Failed to update user status. User may not exist.");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("enabled", newStatus);
        result.put("disabled", disable);
        result.put("reason", reason);
        result.put("updatedAt", LocalDateTime.now());
        result.put("action", disable ? "DISABLED" : "ENABLED");
        
        return result;
    }

    /**
     * Get current subscription details for a user
     */
    private Map<String, Object> getCurrentSubscriptionDetails(Integer userId) {
        Map<String, Object> subscriptionDetails = new HashMap<>();
        
        try {
            List<Object[]> currentSubscriptions = userRepository.findCurrentSubscription(userId);
            
            if (!currentSubscriptions.isEmpty()) {
                Object[] currentSub = currentSubscriptions.get(0); // Get the most recent active subscription
                
                subscriptionDetails.put("hasActiveSubscription", true);
                subscriptionDetails.put("subscriptionCount", currentSubscriptions.size());
                
                // Extract subscription plan details
                // Object[] format: [us.id, us.startDate, us.endDate, us.isActive, s.id, s.name, s.price, s.dateType]
                subscriptionDetails.put("subscriptionId", currentSub[0]);
                subscriptionDetails.put("startDate", currentSub[1]);
                subscriptionDetails.put("endDate", currentSub[2]);
                subscriptionDetails.put("isActive", currentSub[3]);
                subscriptionDetails.put("planId", currentSub[4]);
                subscriptionDetails.put("planName", currentSub[5]); 
                subscriptionDetails.put("planPrice", currentSub[6]);
                subscriptionDetails.put("planDateType", currentSub[7]);
                
            } else {
                subscriptionDetails.put("hasActiveSubscription", false);
                subscriptionDetails.put("subscriptionCount", 0);
                subscriptionDetails.put("planName", null);
            }
            
            // Get longest subscription details
            List<Object[]> allSubscriptions = userRepository.findAllUserSubscriptions(userId);
            if (!allSubscriptions.isEmpty()) {
                Object[] longestSub = allSubscriptions.get(0); // First one is longest due to ORDER BY
                
                subscriptionDetails.put("longestSubscription", Map.of(
                    "planName", longestSub[5], // Longest subscription plan name
                    "durationDays", longestSub[8],
                    "startDate", longestSub[1],
                    "endDate", longestSub[2]
                ));
            }
            
        } catch (Exception e) {
            subscriptionDetails.put("hasActiveSubscription", false);
            subscriptionDetails.put("subscriptionCount", 0);
            subscriptionDetails.put("planName", null);
            subscriptionDetails.put("error", "Could not fetch subscription details: " + e.getMessage());
        }
        
        return subscriptionDetails;
    }

    @Override
    public Map<String, Long> getNewUsersCountByPeriod(String period) {
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "month":
                // Get current month data
                YearMonth currentMonth = YearMonth.now();
                LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
                
                long currentMonthUsers = allUsers.stream()
                        .filter(user -> user.getCreatedAt() != null && 
                                user.getCreatedAt().isAfter(startOfMonth) && 
                                user.getCreatedAt().isBefore(now))
                        .count();
                result.put("current_month", currentMonthUsers);
                break;
                
            case "quarter":
                // Current quarter (3 months)
                LocalDateTime threeMonthsAgo = now.minusMonths(3);
                
                long quarterlyUsers = allUsers.stream()
                        .filter(user -> user.getCreatedAt() != null && 
                                user.getCreatedAt().isAfter(threeMonthsAgo) && 
                                user.getCreatedAt().isBefore(now))
                        .count();
                result.put("last_3_months", quarterlyUsers);
                
                // Monthly breakdown for the quarter
                for (int i = 0; i < 3; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    long monthlyUsers = allUsers.stream()
                            .filter(user -> user.getCreatedAt() != null && 
                                    user.getCreatedAt().isAfter(startOfPastMonth) && 
                                    user.getCreatedAt().isBefore(endOfPastMonth.plusDays(1)))
                            .count();
                    result.put(month.toString(), monthlyUsers);
                }
                break;
                
            case "year":
                // Current year
                LocalDateTime oneYearAgo = now.minusYears(1);
                
                long yearlyUsers = allUsers.stream()
                        .filter(user -> user.getCreatedAt() != null && 
                                user.getCreatedAt().isAfter(oneYearAgo) && 
                                user.getCreatedAt().isBefore(now))
                        .count();
                result.put("last_12_months", yearlyUsers);
                
                // Monthly breakdown for the year
                for (int i = 0; i < 12; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    long monthlyUsers = allUsers.stream()
                            .filter(user -> user.getCreatedAt() != null && 
                                    user.getCreatedAt().isAfter(startOfPastMonth) && 
                                    user.getCreatedAt().isBefore(endOfPastMonth.plusDays(1)))
                            .count();
                    result.put(month.toString(), monthlyUsers);
                }
                break;
                
            default:
                throw new BadRequestException("Invalid period. Use 'month', 'quarter', or 'year'");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getWeeklyUserAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        
        // Get current week range
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday
        LocalDate weekEnd = weekStart.plusDays(6); // Sunday
        
        LocalDateTime weekStartDateTime = weekStart.atStartOfDay();
        LocalDateTime weekEndDateTime = weekEnd.atTime(23, 59, 59);
        
        // Get users registered in this week
        List<User> weekUsers = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null &&
                        user.getCreatedAt().isAfter(weekStartDateTime) &&
                        user.getCreatedAt().isBefore(weekEndDateTime.plusDays(1)))
                .collect(Collectors.toList());
        
        // Count subscribed users in this week
        long subscribedCount = weekUsers.stream()
                .filter(this::isUserSubscribed)
                .count();
        
        int totalNewUsers = weekUsers.size();
        double subscriptionRate = totalNewUsers > 0 ? 
            Math.round((subscribedCount * 100.0 / totalNewUsers) * 100.0) / 100.0 : 0.0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("week_period", weekStart.toString() + " to " + weekEnd.toString());
        response.put("total_new_users", totalNewUsers);
        response.put("subscribed_users", subscribedCount);
        response.put("non_subscribed_users", totalNewUsers - subscribedCount);
        response.put("subscription_rate", subscriptionRate);
        response.put("generated_at", now);
        
        return response;
    }

    @Override
    public Map<String, Object> getMonthlyUserAnalytics(Integer year) {
        int targetYear = year != null ? year : LocalDateTime.now().getYear();
        
        Map<String, Object> response = new HashMap<>();
        response.put("year", targetYear);
        response.put("generated_at", LocalDateTime.now());
        
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        int totalNewUsers = 0;
        int totalSubscribedUsers = 0;
        
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(targetYear, month);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            // Get users registered in this month
            List<User> monthUsers = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt() != null &&
                            user.getCreatedAt().isAfter(startOfMonth) &&
                            user.getCreatedAt().isBefore(endOfMonth.plusDays(1)))
                    .collect(Collectors.toList());
            
            // Count subscribed users in this month
            long subscribedCount = monthUsers.stream()
                    .filter(this::isUserSubscribed)
                    .count();
            
            int monthNewUsers = monthUsers.size();
            totalNewUsers += monthNewUsers;
            totalSubscribedUsers += subscribedCount;
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month_number", month);
            monthData.put("month_name", yearMonth.getMonth().toString());
            monthData.put("month_year", yearMonth.toString());
            monthData.put("total_new_users", monthNewUsers);
            monthData.put("subscribed_users", subscribedCount);
            monthData.put("non_subscribed_users", monthNewUsers - subscribedCount);
            monthData.put("subscription_rate", monthNewUsers > 0 ? 
                Math.round((subscribedCount * 100.0 / monthNewUsers) * 100.0) / 100.0 : 0.0);
            
            monthlyData.add(monthData);
        }
        
        response.put("monthly_breakdown", monthlyData);
        response.put("year_summary", Map.of(
            "total_new_users", totalNewUsers,
            "total_subscribed_users", totalSubscribedUsers,
            "total_non_subscribed_users", totalNewUsers - totalSubscribedUsers,
            "overall_subscription_rate", totalNewUsers > 0 ? 
                Math.round((totalSubscribedUsers * 100.0 / totalNewUsers) * 100.0) / 100.0 : 0.0
        ));
        
        return response;
    }

    @Override
    public Map<String, Object> getYearlyUserAnalytics(int futureYears) {
        int currentYear = LocalDateTime.now().getYear();
        
        Map<String, Object> response = new HashMap<>();
        response.put("current_year", currentYear);
        response.put("generated_at", LocalDateTime.now());
        
        List<Map<String, Object>> yearlyData = new ArrayList<>();
        
        // Historical data for current year and past years, plus future projections
        int startYear = currentYear - 2; // Show 2 years back
        int endYear = currentYear + futureYears;
        
        for (int year = startYear; year <= endYear; year++) {
            boolean isProjection = year > currentYear;
            Map<String, Object> yearData = getYearUserData(year, isProjection);
            yearlyData.add(yearData);
        }
        
        response.put("yearly_breakdown", yearlyData);
        
        return response;
    }

    @Override
    public Map<String, Object> getComprehensiveUserAnalytics() {
        Map<String, Object> response = new HashMap<>();
        response.put("generated_at", LocalDateTime.now());
        
        // Get all analytics components
        response.put("weekly_analytics", getWeeklyUserAnalytics());
        response.put("monthly_analytics", getMonthlyUserAnalytics(null));
        response.put("subscription_breakdown", getUserSubscriptionBreakdown());
        response.put("growth_trends", getUserGrowthTrends());
        
        return response;
    }

    @Override
    public Map<String, Object> getUserSubscriptionBreakdown() {
        List<User> allUsers = userRepository.findAll();
        
        long totalUsers = allUsers.size();
        long subscribedUsers = allUsers.stream()
                .filter(this::isUserSubscribed)
                .count();
        long nonSubscribedUsers = totalUsers - subscribedUsers;
        
        Map<String, Object> response = new HashMap<>();
        response.put("total_users", totalUsers);
        response.put("subscribed_users", subscribedUsers);
        response.put("non_subscribed_users", nonSubscribedUsers);
        response.put("subscription_rate", totalUsers > 0 ? 
            Math.round((subscribedUsers * 100.0 / totalUsers) * 100.0) / 100.0 : 0.0);
        response.put("generated_at", LocalDateTime.now());
        
        return response;
    }

    @Override
    public Map<String, Object> getUserGrowthTrends() {
        Map<String, Object> response = new HashMap<>();
        response.put("generated_at", LocalDateTime.now());
        
        // Calculate monthly growth for last 12 months
        List<Map<String, Object>> monthlyGrowth = new ArrayList<>();
        
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            YearMonth previousMonth = month.minusMonths(1);
            
            long monthUsers = getNewUsersCountByPeriodForMonth(month);
            long previousMonthUsers = getNewUsersCountByPeriodForMonth(previousMonth);
            
            double growthRate = previousMonthUsers > 0 ? 
                ((monthUsers - previousMonthUsers) * 100.0 / previousMonthUsers) : 0.0;
            
            monthlyGrowth.add(Map.of(
                "month", month.toString(),
                "new_users", monthUsers,
                "previous_month_users", previousMonthUsers,
                "growth_rate", Math.round(growthRate * 100.0) / 100.0,
                "trend", growthRate > 0 ? "INCREASING" : growthRate < 0 ? "DECREASING" : "STABLE"
            ));
        }
        
        response.put("monthly_growth_trends", monthlyGrowth);
        
        // Calculate average growth rate
        double avgGrowthRate = monthlyGrowth.stream()
                .mapToDouble(month -> (Double) month.get("growth_rate"))
                .average()
                .orElse(0.0);
        
        response.put("analytics", Map.of(
            "average_monthly_growth_rate", Math.round(avgGrowthRate * 100.0) / 100.0,
            "growth_stability", avgGrowthRate > 10 ? "HIGH_GROWTH" : 
                               avgGrowthRate > 0 ? "STEADY_GROWTH" : "DECLINING",
            "recommendation", avgGrowthRate < 0 ? "IMPLEMENT_RETENTION_STRATEGIES" : 
                             avgGrowthRate < 5 ? "ENHANCE_MARKETING_EFFORTS" : "MAINTAIN_CURRENT_STRATEGY"
        ));
        
        return response;
    }

    // Helper methods
    private boolean isUserSubscribed(User user) {
        try {
            // Check if user has any active subscription by querying the database directly
            return userRepository.findCurrentSubscription(user.getId()).size() > 0;
        } catch (Exception e) {
            return false; // Default to not subscribed if there's an error
        }
    }

    private Map<String, Object> getYearUserData(int year, boolean isProjection) {
        Map<String, Object> yearData = new HashMap<>();
        yearData.put("year", year);
        
        if (!isProjection) {
            // Historical data
            LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            
            List<User> yearUsers = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt() != null &&
                            user.getCreatedAt().isAfter(startOfYear) &&
                            user.getCreatedAt().isBefore(endOfYear.plusDays(1)))
                    .collect(Collectors.toList());
            
            long subscribedCount = yearUsers.stream()
                    .filter(this::isUserSubscribed)
                    .count();
            
            int totalUsers = yearUsers.size();
            double subscriptionRate = totalUsers > 0 ? 
                Math.round((subscribedCount * 100.0 / totalUsers) * 100.0) / 100.0 : 0.0;
            
            yearData.put("total_new_users", totalUsers);
            yearData.put("subscribed_users", subscribedCount);
            yearData.put("non_subscribed_users", totalUsers - subscribedCount);
            yearData.put("subscription_rate", subscriptionRate);
        } else {
            // Projected data based on current trends - return 0 for future years
            yearData.put("total_new_users", 0);
            yearData.put("subscribed_users", 0);
            yearData.put("non_subscribed_users", 0);
            yearData.put("subscription_rate", 0.0);
        }
        
        return yearData;
    }

    private long getNewUsersCountByPeriodForMonth(YearMonth month) {
        List<User> allUsers = userRepository.findAll();
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        
        return allUsers.stream()
                .filter(user -> user.getCreatedAt() != null &&
                        user.getCreatedAt().isAfter(startOfMonth) &&
                        user.getCreatedAt().isBefore(endOfMonth))
                .count();
    }

    @Override
    @Transactional
    public Map<String, Object> updateUserRole(Integer userId, String newRole) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with ID: " + userId));
        
        // Validate role - only USER and ARTIST are allowed
        EnumUserRole enumRole;
        try {
            enumRole = EnumUserRole.valueOf(newRole.toUpperCase());
            if (enumRole != EnumUserRole.USER && enumRole != EnumUserRole.ARTIST) {
                throw new BadRequestException("Invalid role: " + newRole + ". Only USER and ARTIST roles can be assigned");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + newRole + ". Valid roles are: USER, ARTIST");
        }
        
        // Get the role entity
        Role role = roleRepository.findByName(enumRole)
                .orElseThrow(() -> new BadRequestException("Role not found: " + newRole));
        
        // Store previous roles for response
        List<String> previousRoles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());
        
        // Update user roles - replace all roles with the new one (using List instead of Set)
        List<Role> newRoles = new ArrayList<>();
        newRoles.add(role);
        user.setRoles(newRoles);
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save user
        User updatedUser = userRepository.save(user);
        
        // Prepare response
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", user.getName());
        result.put("email", user.getEmail());
        result.put("previousRoles", previousRoles);
        result.put("newRole", newRole.toUpperCase());
        result.put("updatedAt", LocalDateTime.now());
        result.put("success", true);
        result.put("message", "User role updated successfully");
        
        return result;
    }
} 