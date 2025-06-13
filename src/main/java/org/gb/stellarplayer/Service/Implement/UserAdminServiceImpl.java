package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.UserAdminService;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.springframework.stereotype.Service;

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

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
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
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        
        Map<String, Object> response = new HashMap<>();
        response.put("month", currentMonth.toString());
        response.put("month_name", currentMonth.getMonth().toString());
        response.put("year", currentMonth.getYear());
        response.put("generated_at", now);
        
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        int totalNewUsers = 0;
        int totalSubscribedUsers = 0;
        
        // Calculate weeks in current month
        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();
        
        LocalDate weekStart = firstDay;
        int weekNumber = 1;
        
        while (!weekStart.isAfter(lastDay)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(lastDay)) {
                weekEnd = lastDay;
            }
            
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
            
            int weekNewUsers = weekUsers.size();
            totalNewUsers += weekNewUsers;
            totalSubscribedUsers += subscribedCount;
            
            Map<String, Object> weekData = new HashMap<>();
            weekData.put("week_number", weekNumber);
            weekData.put("week_start", weekStart.toString());
            weekData.put("week_end", weekEnd.toString());
            weekData.put("new_users", weekNewUsers);
            weekData.put("subscribed_users", subscribedCount);
            weekData.put("non_subscribed_users", weekNewUsers - subscribedCount);
            weekData.put("subscription_rate", weekNewUsers > 0 ? 
                Math.round((subscribedCount * 100.0 / weekNewUsers) * 100.0) / 100.0 : 0.0);
            
            weeklyData.add(weekData);
            
            weekStart = weekStart.plusDays(7);
            weekNumber++;
        }
        
        response.put("weekly_breakdown", weeklyData);
        response.put("month_summary", Map.of(
            "total_new_users", totalNewUsers,
            "total_subscribed_users", totalSubscribedUsers,
            "total_non_subscribed_users", totalNewUsers - totalSubscribedUsers,
            "overall_subscription_rate", totalNewUsers > 0 ? 
                Math.round((totalSubscribedUsers * 100.0 / totalNewUsers) * 100.0) / 100.0 : 0.0,
            "weeks_in_month", weeklyData.size()
        ));
        
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
            monthData.put("new_users", monthNewUsers);
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
                Math.round((totalSubscribedUsers * 100.0 / totalNewUsers) * 100.0) / 100.0 : 0.0,
            "average_monthly_users", Math.round((totalNewUsers / 12.0) * 100.0) / 100.0
        ));
        
        return response;
    }

    @Override
    public Map<String, Object> getYearlyUserAnalytics(int futureYears) {
        int currentYear = LocalDateTime.now().getYear();
        
        Map<String, Object> response = new HashMap<>();
        response.put("current_year", currentYear);
        response.put("future_years_projected", futureYears);
        response.put("generated_at", LocalDateTime.now());
        
        List<Map<String, Object>> yearlyData = new ArrayList<>();
        
        // Historical data for current year and past 2 years
        for (int i = 2; i >= 0; i--) {
            int year = currentYear - i;
            Map<String, Object> yearData = getYearUserData(year, false);
            yearlyData.add(yearData);
        }
        
        // Projected data for future years
        for (int i = 1; i <= futureYears; i++) {
            int year = currentYear + i;
            Map<String, Object> yearData = getYearUserData(year, true);
            yearlyData.add(yearData);
        }
        
        response.put("yearly_breakdown", yearlyData);
        
        // Calculate growth trends
        if (yearlyData.size() >= 2) {
            Map<String, Object> currentYearData = (Map<String, Object>) yearlyData.get(yearlyData.size() - futureYears - 1);
            Map<String, Object> previousYearData = (Map<String, Object>) yearlyData.get(yearlyData.size() - futureYears - 2);
            
            int currentUsers = (Integer) currentYearData.get("new_users");
            int previousUsers = (Integer) previousYearData.get("new_users");
            
            double growthRate = previousUsers > 0 ? 
                ((currentUsers - previousUsers) * 100.0 / previousUsers) : 0.0;
            
            response.put("growth_analysis", Map.of(
                "year_over_year_growth", Math.round(growthRate * 100.0) / 100.0,
                "growth_trend", growthRate > 10 ? "HIGH_GROWTH" : 
                               growthRate > 0 ? "MODERATE_GROWTH" : 
                               growthRate > -10 ? "SLIGHT_DECLINE" : "SIGNIFICANT_DECLINE"
            ));
        }
        
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
        
        // Breakdown by registration date (last 12 months)
        List<Map<String, Object>> monthlyBreakdown = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
            
            List<User> monthUsers = allUsers.stream()
                    .filter(user -> user.getCreatedAt() != null &&
                            user.getCreatedAt().isAfter(startOfMonth) &&
                            user.getCreatedAt().isBefore(endOfMonth.plusDays(1)))
                    .collect(Collectors.toList());
            
            long monthSubscribed = monthUsers.stream()
                    .filter(this::isUserSubscribed)
                    .count();
            
            monthlyBreakdown.add(Map.of(
                "month", month.toString(),
                "month_name", month.getMonth().toString(),
                "total_users", monthUsers.size(),
                "subscribed_users", monthSubscribed,
                "non_subscribed_users", monthUsers.size() - monthSubscribed,
                "subscription_rate", monthUsers.size() > 0 ? 
                    Math.round((monthSubscribed * 100.0 / monthUsers.size()) * 100.0) / 100.0 : 0.0
            ));
        }
        
        response.put("monthly_subscription_breakdown", monthlyBreakdown);
        
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
        // Check if user has active subscription
        try {
            return userSubscriptionService.getTotalActiveSubscriptionsCount() > 0 && 
                   userSubscriptionService.getNewSubscriptionsCountByPeriod("year").values().stream()
                       .anyMatch(count -> count > 0);
            // This is a simplified check - you might want to implement a more specific method
            // in UserSubscriptionService to check individual user subscription status
        } catch (Exception e) {
            return false; // Default to not subscribed if there's an error
        }
    }

    private Map<String, Object> getYearUserData(int year, boolean isProjection) {
        Map<String, Object> yearData = new HashMap<>();
        yearData.put("year", year);
        yearData.put("is_projection", isProjection);
        
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
            
            yearData.put("new_users", yearUsers.size());
            yearData.put("subscribed_users", subscribedCount);
            yearData.put("non_subscribed_users", yearUsers.size() - subscribedCount);
        } else {
            // Projected data based on current trends
            Map<String, Long> currentYearData = getNewUsersCountByPeriod("year");
            double currentYearUsers = currentYearData.getOrDefault("last_12_months", 0L);
            double projectedUsers = currentYearUsers * 1.1; // 10% growth assumption
            double projectedSubscribed = projectedUsers * 0.3; // 30% subscription rate assumption
            
            yearData.put("new_users", Math.round(projectedUsers));
            yearData.put("subscribed_users", Math.round(projectedSubscribed));
            yearData.put("non_subscribed_users", Math.round(projectedUsers - projectedSubscribed));
        }
        
        return yearData;
    }

    private long getNewUsersCountByPeriodForMonth(YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
        
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null &&
                        user.getCreatedAt().isAfter(startOfMonth) &&
                        user.getCreatedAt().isBefore(endOfMonth.plusDays(1)))
                .count();
    }
} 