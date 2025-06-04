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
import java.util.HashMap;
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
} 