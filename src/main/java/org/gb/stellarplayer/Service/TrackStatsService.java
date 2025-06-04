package org.gb.stellarplayer.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service for track listening statistics and analytics
 */
public interface TrackStatsService {
    
    /**
     * Get detailed statistics for a track within a date range
     * @param trackId The track ID
     * @param startDate Optional start date for the range
     * @param endDate Optional end date for the range
     * @return Map of statistics
     */
    Map<String, Object> getDetailedTrackStats(int trackId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get weekly play counts for a track
     * @param trackId The track ID
     * @param startDate Optional start date (defaults to current week)
     * @return Map of date to play count
     */
    Map<String, Long> getWeeklyPlayCounts(int trackId, LocalDate startDate);
    
    /**
     * Get monthly play counts for a track
     * @param trackId The track ID
     * @param year Optional year (defaults to current year)
     * @return Map of month to play count
     */
    Map<String, Long> getMonthlyPlayCounts(int trackId, Integer year);
    
    /**
     * Get user engagement statistics for a track
     * @param trackId The track ID
     * @return Map of engagement metrics
     */
    Map<String, Object> getTrackEngagementStats(int trackId);
} 