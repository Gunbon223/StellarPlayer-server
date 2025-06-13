package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.OrderRepository;
import org.gb.stellarplayer.Service.RevenueStatisticsService;
import org.gb.stellarplayer.Service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of RevenueStatisticsService
 */
@Service
@RequiredArgsConstructor
public class RevenueStatisticsServiceImpl implements RevenueStatisticsService {

    private final OrderRepository orderRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public Map<String, Object> calculateSubscriptionRevenue(int subscriptionId, String period) {
        Subscription subscription = subscriptionService.getSubscriptionById(subscriptionId);
        List<Order> allPaidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals("PAID"))
                .filter(order -> order.getSubscription() != null && order.getSubscription().getId() == subscriptionId)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("subscription_id", subscriptionId);
        response.put("subscription_name", subscription.getName());
        response.put("period", period);
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "month":
                // Current month
                YearMonth currentMonth = YearMonth.now();
                LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
                
                calculateRevenueForPeriod(allPaidOrders, startOfMonth, now, response, "current_month");
                break;
                
            case "quarter":
                // Last 3 months
                LocalDateTime threeMonthsAgo = now.minusMonths(3);
                
                calculateRevenueForPeriod(allPaidOrders, threeMonthsAgo, now, response, "last_3_months");
                
                // Monthly breakdown for quarter
                Map<String, Object> monthlyBreakdown = new HashMap<>();
                for (int i = 0; i < 3; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    Map<String, Object> monthData = new HashMap<>();
                    calculateRevenueForPeriod(allPaidOrders, startOfPastMonth, endOfPastMonth, monthData, "revenue");
                    monthlyBreakdown.put(month.toString(), monthData);
                }
                response.put("monthly_breakdown", monthlyBreakdown);
                break;
                
            case "year":
                // Last 12 months
                LocalDateTime oneYearAgo = now.minusYears(1);
                
                calculateRevenueForPeriod(allPaidOrders, oneYearAgo, now, response, "last_12_months");
                
                // Monthly breakdown for year
                Map<String, Object> yearlyBreakdown = new HashMap<>();
                for (int i = 0; i < 12; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    Map<String, Object> monthData = new HashMap<>();
                    calculateRevenueForPeriod(allPaidOrders, startOfPastMonth, endOfPastMonth, monthData, "revenue");
                    yearlyBreakdown.put(month.toString(), monthData);
                }
                response.put("monthly_breakdown", yearlyBreakdown);
                break;
                
            default:
                throw new BadRequestException("Invalid period. Use 'month', 'quarter', or 'year'");
        }
        
        return response;
    }

    @Override
    public Map<String, Object> calculateTotalRevenue(String period) {
        List<Order> allPaidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals("PAID"))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("period", period);
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "month":
                // Current month
                YearMonth currentMonth = YearMonth.now();
                LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
                
                calculateRevenueForPeriod(allPaidOrders, startOfMonth, now, response, "current_month");
                break;
                
            case "quarter":
                // Last 3 months
                LocalDateTime threeMonthsAgo = now.minusMonths(3);
                
                calculateRevenueForPeriod(allPaidOrders, threeMonthsAgo, now, response, "last_3_months");
                
                // Monthly breakdown for quarter
                Map<String, Object> monthlyBreakdown = new HashMap<>();
                for (int i = 0; i < 3; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    Map<String, Object> monthData = new HashMap<>();
                    calculateRevenueForPeriod(allPaidOrders, startOfPastMonth, endOfPastMonth, monthData, "revenue");
                    monthlyBreakdown.put(month.toString(), monthData);
                }
                response.put("monthly_breakdown", monthlyBreakdown);
                break;
                
            case "year":
                // Last 12 months
                LocalDateTime oneYearAgo = now.minusYears(1);
                
                calculateRevenueForPeriod(allPaidOrders, oneYearAgo, now, response, "last_12_months");
                
                // Monthly breakdown for year
                Map<String, Object> yearlyBreakdown = new HashMap<>();
                for (int i = 0; i < 12; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    Map<String, Object> monthData = new HashMap<>();
                    calculateRevenueForPeriod(allPaidOrders, startOfPastMonth, endOfPastMonth, monthData, "revenue");
                    yearlyBreakdown.put(month.toString(), monthData);
                }
                response.put("monthly_breakdown", yearlyBreakdown);
                break;
                
            default:
                throw new BadRequestException("Invalid period. Use 'month', 'quarter', or 'year'");
        }
        
        // Add subscription-specific breakdown
        Map<String, Object> subscriptionBreakdown = new HashMap<>();
        List<Subscription> allSubscriptions = subscriptionService.getSubscription();
        
        for (Subscription subscription : allSubscriptions) {
            List<Order> subscriptionOrders = allPaidOrders.stream()
                    .filter(order -> order.getSubscription() != null && 
                            order.getSubscription().getId().equals(subscription.getId()))
                    .collect(Collectors.toList());
            
            Map<String, Object> subscriptionData = new HashMap<>();
            calculateRevenueForPeriod(subscriptionOrders, 
                    getPeriodStartDate(period), now, subscriptionData, "revenue");
            
            subscriptionBreakdown.put(subscription.getName(), subscriptionData);
        }
        
        response.put("subscription_breakdown", subscriptionBreakdown);
        
        return response;
    }
    
    /**
     * Calculate revenue for a specific period
     * @param orders List of paid orders
     * @param startDate Start date of period
     * @param endDate End date of period
     * @param response Response map to populate
     * @param keyPrefix Key prefix for response
     */
    private void calculateRevenueForPeriod(List<Order> orders, LocalDateTime startDate, 
            LocalDateTime endDate, Map<String, Object> response, String keyPrefix) {
        List<Order> periodOrders = orders.stream()
                .filter(order -> order.getPaidAt() != null && 
                        order.getPaidAt().isAfter(startDate) && 
                        order.getPaidAt().isBefore(endDate))
                .collect(Collectors.toList());
        
        double totalRevenue = periodOrders.stream()
                .mapToDouble(Order::getFinalAmount)
                .sum();
        
        double originalRevenue = periodOrders.stream()
                .mapToDouble(Order::getOriginalAmount)
                .sum();
        
        double discountAmount = periodOrders.stream()
                .mapToDouble(Order::getDiscountAmount)
                .sum();
        
        long orderCount = periodOrders.size();
        
        response.put(keyPrefix + "_total_revenue", totalRevenue);
        response.put(keyPrefix + "_original_revenue", originalRevenue);
        response.put(keyPrefix + "_discount_amount", discountAmount);
        response.put(keyPrefix + "_order_count", orderCount);
    }
    
    /**
     * Get start date for a specific period
     * @param period Period (month, quarter, year)
     * @return Start date
     */
    private LocalDateTime getPeriodStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "month":
                return YearMonth.now().atDay(1).atStartOfDay();
            case "quarter":
                return now.minusMonths(3);
            case "year":
                return now.minusYears(1);
            default:
                return now.minusMonths(1);
        }
    }

    @Override
    public Map<String, Object> getMonthlyRevenueBreakdown(int subscriptionId, int months) {
        Subscription subscription = subscriptionService.getSubscriptionById(subscriptionId);
        List<Order> allPaidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals("PAID"))
                .filter(order -> order.getSubscription() != null && order.getSubscription().getId() == subscriptionId)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("subscription_id", subscriptionId);
        response.put("subscription_name", subscription.getName());
        response.put("months_requested", months);
        response.put("generated_at", LocalDateTime.now());
        
        // Create monthly breakdown
        Map<String, Object> monthlyData = new LinkedHashMap<>();
        double totalRevenue = 0;
        int totalOrders = 0;
        
        for (int i = 0; i < months; i++) {
            YearMonth month = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
            
            Map<String, Object> monthData = new HashMap<>();
            calculateRevenueForPeriod(allPaidOrders, startOfMonth, endOfMonth, monthData, "");
            
            // Add formatted month name for better readability
            monthData.put("month_name", month.getMonth().toString());
            monthData.put("year", month.getYear());
            monthData.put("month_year", month.toString());
            
            double monthRevenue = (Double) monthData.getOrDefault("_total_revenue", 0.0);
            int monthOrders = ((Long) monthData.getOrDefault("_order_count", 0L)).intValue();
            
            totalRevenue += monthRevenue;
            totalOrders += monthOrders;
            
            monthlyData.put(month.toString(), monthData);
        }
        
        response.put("monthly_breakdown", monthlyData);
        response.put("summary", Map.of(
            "total_revenue_" + months + "_months", totalRevenue,
            "total_orders_" + months + "_months", totalOrders,
            "average_monthly_revenue", totalRevenue / months,
            "average_monthly_orders", (double) totalOrders / months
        ));
        
        return response;
    }

    @Override
    public Map<String, Object> getDetailedRevenueAnalytics(int subscriptionId) {
        Subscription subscription = subscriptionService.getSubscriptionById(subscriptionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("subscription_id", subscriptionId);
        response.put("subscription_name", subscription.getName());
        response.put("generated_at", LocalDateTime.now());
        
        // Get 12 months of data
        Map<String, Object> monthlyData = getMonthlyRevenueBreakdown(subscriptionId, 12);
        response.put("monthly_breakdown", monthlyData.get("monthly_breakdown"));
        
        // Calculate trends and analytics
        List<Order> allPaidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals("PAID"))
                .filter(order -> order.getSubscription() != null && order.getSubscription().getId() == subscriptionId)
                .collect(Collectors.toList());
        
        // Current month vs previous month comparison
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        
        Map<String, Object> currentMonthData = new HashMap<>();
        calculateRevenueForPeriod(allPaidOrders, 
            currentMonth.atDay(1).atStartOfDay(), 
            currentMonth.atEndOfMonth().atTime(23, 59, 59), 
            currentMonthData, "current");
            
        Map<String, Object> previousMonthData = new HashMap<>();
        calculateRevenueForPeriod(allPaidOrders, 
            previousMonth.atDay(1).atStartOfDay(), 
            previousMonth.atEndOfMonth().atTime(23, 59, 59), 
            previousMonthData, "previous");
        
        double currentRevenue = (Double) currentMonthData.getOrDefault("current_total_revenue", 0.0);
        double previousRevenue = (Double) previousMonthData.getOrDefault("previous_total_revenue", 0.0);
        
        double growthRate = previousRevenue > 0 ? ((currentRevenue - previousRevenue) / previousRevenue) * 100 : 0;
        
        response.put("month_over_month_analysis", Map.of(
            "current_month", currentMonth.toString(),
            "current_month_revenue", currentRevenue,
            "previous_month", previousMonth.toString(),
            "previous_month_revenue", previousRevenue,
            "growth_rate_percentage", growthRate,
            "trend", growthRate > 0 ? "INCREASING" : growthRate < 0 ? "DECREASING" : "STABLE"
        ));
        
        // Best and worst performing months
        Map<String, Object> monthlyBreakdown = (Map<String, Object>) monthlyData.get("monthly_breakdown");
        String bestMonth = "";
        String worstMonth = "";
        double highestRevenue = -1;
        double lowestRevenue = Double.MAX_VALUE;
        
        for (Map.Entry<String, Object> entry : monthlyBreakdown.entrySet()) {
            Map<String, Object> data = (Map<String, Object>) entry.getValue();
            double revenue = (Double) data.getOrDefault("_total_revenue", 0.0);
            
            if (revenue > highestRevenue) {
                highestRevenue = revenue;
                bestMonth = entry.getKey();
            }
            if (revenue < lowestRevenue && revenue > 0) {
                lowestRevenue = revenue;
                worstMonth = entry.getKey();
            }
        }
        
        response.put("performance_analysis", Map.of(
            "best_month", bestMonth,
            "best_month_revenue", highestRevenue,
            "worst_month", worstMonth,
            "worst_month_revenue", lowestRevenue,
            "revenue_variance", highestRevenue - lowestRevenue
        ));
        
        return response;
    }

    @Override
    public Map<String, Object> getAllSubscriptionsMonthlyRevenue(int months) {
        List<Subscription> allSubscriptions = subscriptionService.getSubscription();
        
        Map<String, Object> response = new HashMap<>();
        response.put("months_requested", months);
        response.put("total_subscriptions", allSubscriptions.size());
        response.put("generated_at", LocalDateTime.now());
        
        Map<String, Object> subscriptionBreakdown = new HashMap<>();
        Map<String, Object> monthlyTotals = new LinkedHashMap<>();
        
        // Initialize monthly totals
        for (int i = 0; i < months; i++) {
            YearMonth month = YearMonth.now().minusMonths(i);
            monthlyTotals.put(month.toString(), Map.of(
                "total_revenue", 0.0,
                "total_orders", 0L,
                "subscriptions_with_revenue", 0
            ));
        }
        
        // Calculate for each subscription
        for (Subscription subscription : allSubscriptions) {
            Map<String, Object> subscriptionMonthly = getMonthlyRevenueBreakdown(subscription.getId(), months);
            subscriptionBreakdown.put(subscription.getName(), subscriptionMonthly);
            
            // Add to monthly totals
            Map<String, Object> monthlyData = (Map<String, Object>) subscriptionMonthly.get("monthly_breakdown");
            for (Map.Entry<String, Object> monthEntry : monthlyData.entrySet()) {
                Map<String, Object> monthData = (Map<String, Object>) monthEntry.getValue();
                Map<String, Object> totalData = (Map<String, Object>) monthlyTotals.get(monthEntry.getKey());
                
                double currentTotal = (Double) totalData.get("total_revenue");
                long currentOrders = (Long) totalData.get("total_orders");
                int currentSubscriptions = (Integer) totalData.get("subscriptions_with_revenue");
                
                double monthRevenue = (Double) monthData.getOrDefault("_total_revenue", 0.0);
                long monthOrders = (Long) monthData.getOrDefault("_order_count", 0L);
                
                monthlyTotals.put(monthEntry.getKey(), Map.of(
                    "total_revenue", currentTotal + monthRevenue,
                    "total_orders", currentOrders + monthOrders,
                    "subscriptions_with_revenue", monthRevenue > 0 ? currentSubscriptions + 1 : currentSubscriptions
                ));
            }
        }
        
        response.put("subscription_breakdown", subscriptionBreakdown);
        response.put("monthly_totals", monthlyTotals);
        
        return response;
    }

    @Override
    public Map<String, Object> getMonthlySummaryAllSubscriptions(int months) {
        // Get all paid orders
        List<Order> allPaidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals("PAID"))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("months_requested", months);
        response.put("report_type", "Monthly Revenue Summary");
        response.put("generated_at", LocalDateTime.now().toString());
        
        // Create monthly summary data
        Map<String, Object> monthlySummary = new LinkedHashMap<>();
        List<Map<String, Object>> monthlyList = new ArrayList<>();
        
        double grandTotalRevenue = 0;
        long grandTotalOrders = 0;
        double highestMonthRevenue = 0;
        double lowestMonthRevenue = Double.MAX_VALUE;
        String bestMonth = "";
        String worstMonth = "";
        
        for (int i = months - 1; i >= 0; i--) { // Start from oldest month to newest
            YearMonth month = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);
            
            // Get orders for this month
            List<Order> monthOrders = allPaidOrders.stream()
                    .filter(order -> order.getPaidAt() != null && 
                            order.getPaidAt().isAfter(startOfMonth) && 
                            order.getPaidAt().isBefore(endOfMonth.plusDays(1)))
                    .collect(Collectors.toList());
            
            // Calculate monthly totals
            double monthlyRevenue = monthOrders.stream()
                    .mapToDouble(Order::getFinalAmount)
                    .sum();
            
            double monthlyOriginalRevenue = monthOrders.stream()
                    .mapToDouble(Order::getOriginalAmount)
                    .sum();
            
            double monthlyDiscounts = monthOrders.stream()
                    .mapToDouble(Order::getDiscountAmount)
                    .sum();
            
            long monthlyOrderCount = monthOrders.size();
            
            // Track best/worst months
            if (monthlyRevenue > highestMonthRevenue) {
                highestMonthRevenue = monthlyRevenue;
                bestMonth = month.toString();
            }
            if (monthlyRevenue < lowestMonthRevenue && monthlyRevenue > 0) {
                lowestMonthRevenue = monthlyRevenue;
                worstMonth = month.toString();
            }
            
            // Add to grand totals
            grandTotalRevenue += monthlyRevenue;
            grandTotalOrders += monthlyOrderCount;
            
            // Create month data
            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", month.toString());
            monthData.put("month_name", month.getMonth().toString());
            monthData.put("year", month.getYear());
            monthData.put("total_revenue", Math.round(monthlyRevenue * 100.0) / 100.0);
            monthData.put("original_revenue", Math.round(monthlyOriginalRevenue * 100.0) / 100.0);
            monthData.put("total_discounts", Math.round(monthlyDiscounts * 100.0) / 100.0);
            monthData.put("total_orders", monthlyOrderCount);
            monthData.put("average_order_value", monthlyOrderCount > 0 ? 
                Math.round((monthlyRevenue / monthlyOrderCount) * 100.0) / 100.0 : 0.0);
            
            monthlyList.add(monthData);
            monthlySummary.put(month.toString(), monthData);
        }
        
        // Calculate additional analytics
        double averageMonthlyRevenue = grandTotalRevenue / months;
        double averageMonthlyOrders = (double) grandTotalOrders / months;
        
        // Year-over-year comparison (if we have data from 12+ months ago)
        YearMonth currentMonth = YearMonth.now();
        YearMonth sameMonthLastYear = currentMonth.minusYears(1);
        
        double currentMonthRevenue = 0;
        double sameMonthLastYearRevenue = 0;
        
        if (months >= 12) {
            // Get current month revenue
            LocalDateTime currentStart = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime currentEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            currentMonthRevenue = allPaidOrders.stream()
                    .filter(order -> order.getPaidAt() != null && 
                            order.getPaidAt().isAfter(currentStart) && 
                            order.getPaidAt().isBefore(currentEnd.plusDays(1)))
                    .mapToDouble(Order::getFinalAmount)
                    .sum();
            
            // Get same month last year revenue
            LocalDateTime lastYearStart = sameMonthLastYear.atDay(1).atStartOfDay();
            LocalDateTime lastYearEnd = sameMonthLastYear.atEndOfMonth().atTime(23, 59, 59);
            sameMonthLastYearRevenue = allPaidOrders.stream()
                    .filter(order -> order.getPaidAt() != null && 
                            order.getPaidAt().isAfter(lastYearStart) && 
                            order.getPaidAt().isBefore(lastYearEnd.plusDays(1)))
                    .mapToDouble(Order::getFinalAmount)
                    .sum();
        }
        
        double yearOverYearGrowth = sameMonthLastYearRevenue > 0 ? 
            ((currentMonthRevenue - sameMonthLastYearRevenue) / sameMonthLastYearRevenue) * 100 : 0;
        
        // Build response
        response.put("monthly_summary", monthlyList);
        response.put("period_totals", Map.of(
            "total_revenue", Math.round(grandTotalRevenue * 100.0) / 100.0,
            "total_orders", grandTotalOrders,
            "average_monthly_revenue", Math.round(averageMonthlyRevenue * 100.0) / 100.0,
            "average_monthly_orders", Math.round(averageMonthlyOrders * 100.0) / 100.0,
            "highest_month_revenue", Math.round(highestMonthRevenue * 100.0) / 100.0,
            "lowest_month_revenue", lowestMonthRevenue == Double.MAX_VALUE ? 0.0 : 
                Math.round(lowestMonthRevenue * 100.0) / 100.0
        ));
        
        response.put("performance_insights", Map.of(
            "best_performing_month", bestMonth,
            "best_month_revenue", Math.round(highestMonthRevenue * 100.0) / 100.0,
            "worst_performing_month", worstMonth.isEmpty() ? "N/A" : worstMonth,
            "worst_month_revenue", lowestMonthRevenue == Double.MAX_VALUE ? 0.0 : 
                Math.round(lowestMonthRevenue * 100.0) / 100.0,
            "revenue_volatility", Math.round((highestMonthRevenue - 
                (lowestMonthRevenue == Double.MAX_VALUE ? 0 : lowestMonthRevenue)) * 100.0) / 100.0
        ));
        
        if (months >= 12) {
            response.put("year_over_year_analysis", Map.of(
                "current_month", currentMonth.toString(),
                "current_month_revenue", Math.round(currentMonthRevenue * 100.0) / 100.0,
                "same_month_last_year", sameMonthLastYear.toString(),
                "same_month_last_year_revenue", Math.round(sameMonthLastYearRevenue * 100.0) / 100.0,
                "year_over_year_growth_percentage", Math.round(yearOverYearGrowth * 100.0) / 100.0,
                "growth_trend", yearOverYearGrowth > 5 ? "STRONG_GROWTH" : 
                               yearOverYearGrowth > 0 ? "MODERATE_GROWTH" : 
                               yearOverYearGrowth > -5 ? "SLIGHT_DECLINE" : "SIGNIFICANT_DECLINE"
            ));
        }
        
        // Add subscription count info
        List<Subscription> allSubscriptions = subscriptionService.getSubscription();
        response.put("subscription_info", Map.of(
            "total_active_subscriptions", allSubscriptions.size(),
            "subscriptions_with_orders_in_period", allPaidOrders.stream()
                .filter(order -> order.getSubscription() != null)
                .map(order -> order.getSubscription().getId())
                .collect(Collectors.toSet()).size()
        ));
        
        return response;
    }
} 