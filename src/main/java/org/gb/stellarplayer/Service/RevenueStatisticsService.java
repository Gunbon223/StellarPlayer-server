package org.gb.stellarplayer.Service;

import java.util.Map;

/**
 * Service interface for subscription revenue statistics calculations
 */
public interface RevenueStatisticsService {
    
    /**
     * Calculate revenue statistics for a specific subscription by period
     * 
     * @param subscriptionId The subscription ID
     * @param period The period type (month, quarter, year)
     * @return Map containing revenue statistics
     */
    Map<String, Object> calculateSubscriptionRevenue(int subscriptionId, String period);
    
    /**
     * Calculate total revenue statistics across all subscriptions by period
     * 
     * @param period The period type (month, quarter, year)
     * @return Map containing revenue statistics
     */
    Map<String, Object> calculateTotalRevenue(String period);

    /**
     * Get monthly revenue breakdown for a specific subscription
     * 
     * @param subscriptionId The subscription ID
     * @param months Number of months to include (default: 12)
     * @return Map containing monthly revenue breakdown
     */
    Map<String, Object> getMonthlyRevenueBreakdown(int subscriptionId, int months);

    /**
     * Get detailed revenue analytics for a specific subscription
     * 
     * @param subscriptionId The subscription ID
     * @return Map containing detailed analytics including trends and comparisons
     */
    Map<String, Object> getDetailedRevenueAnalytics(int subscriptionId);

    /**
     * Get monthly revenue breakdown for all subscriptions
     * 
     * @param months Number of months to include
     * @return Map containing monthly revenue breakdown for all subscriptions
     */
    Map<String, Object> getAllSubscriptionsMonthlyRevenue(int months);

    /**
     * Get summarized monthly revenue for all subscriptions
     * Returns a clean summary with total orders and revenue per month
     * 
     * @param months Number of months to include
     * @return Map containing summarized monthly revenue data
     */
    Map<String, Object> getMonthlySummaryAllSubscriptions(int months);
} 