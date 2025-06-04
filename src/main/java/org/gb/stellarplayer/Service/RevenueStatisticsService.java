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
} 