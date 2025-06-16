package org.gb.stellarplayer.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

/**
 * Service interface for user admin analytics and management
 */
public interface UserAdminService {
    
    /**
     * Get total users count
     * @return Total count of users
     */
    long getTotalUsersCount();
    
    /**
     * Get all users with pagination and search
     * @param pageable Pagination information
     * @param search Search term for filtering by name or email
     * @return Page of users with their basic information
     */
    Page<Map<String, Object>> getAllUsers(Pageable pageable, String search);
    
    /**
     * Get subscribed users with subscription details
     * @param pageable Pagination information  
     * @param search Search term for filtering by name or email
     * @return Page of subscribed users with subscription pack details and longest subscription info
     */
    Page<Map<String, Object>> getSubscribedUsers(Pageable pageable, String search);
    
    /**
     * Update user status (enable/disable)
     * @param userId User ID to update
     * @param disable True to disable, false to enable
     * @param reason Reason for the action (optional)
     * @return Map containing update result information
     */
    Map<String, Object> updateUserStatus(Integer userId, boolean disable, String reason);
    
    /**
     * Get new users count by period with breakdown
     * @param period The period (month, quarter, year)
     * @return Map containing new users count breakdown
     */
    Map<String, Long> getNewUsersCountByPeriod(String period);
    
    /**
     * Get weekly user analytics for current month
     * @return Map containing weekly user breakdown with subscription status
     */
    Map<String, Object> getWeeklyUserAnalytics();

    /**
     * Get monthly user analytics for specified year
     * @param year Year to analyze (null for current year)
     * @return Map containing monthly user breakdown with subscription status
     */
    Map<String, Object> getMonthlyUserAnalytics(Integer year);

    /**
     * Get yearly user analytics with future projections
     * @param futureYears Number of future years to project
     * @return Map containing yearly user breakdown and projections
     */
    Map<String, Object> getYearlyUserAnalytics(int futureYears);

    /**
     * Get comprehensive user analytics for dashboard
     * @return Map containing comprehensive user analytics
     */
    Map<String, Object> getComprehensiveUserAnalytics();

    /**
     * Get user subscription status breakdown
     * @return Map containing subscription status breakdown
     */
    Map<String, Object> getUserSubscriptionBreakdown();

    /**
     * Get user growth trends and projections
     * @return Map containing growth trends and projections
     */
    Map<String, Object> getUserGrowthTrends();

    /**
     * Update user role
     * @param userId User ID to update
     * @param newRole New role to assign (USER, ARTIST, ADMIN)
     * @return Map containing update result information
     */
    Map<String, Object> updateUserRole(Integer userId, String newRole);
} 