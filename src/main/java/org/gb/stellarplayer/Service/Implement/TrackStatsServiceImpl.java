package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Repository.TrackPlayLogRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Service.TrackStatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TrackStatsServiceImpl implements TrackStatsService {
    
    private final TrackRepository trackRepository;
    private final TrackPlayLogRepository trackPlayLogRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public Map<String, Object> getDetailedTrackStats(int trackId, LocalDate startDate, LocalDate endDate) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));
        
        // Default date range to last 30 days if not specified
        LocalDate start = Optional.ofNullable(startDate)
                .orElse(LocalDate.now().minusDays(30));
        LocalDate end = Optional.ofNullable(endDate)
                .orElse(LocalDate.now());
        
        // Calculate daily play counts (this would involve querying logs)
        Map<String, Long> dailyPlayCounts = getDailyPlayCounts(trackId, start, end);
        
        // Calculate average daily plays
        double avgPlays = dailyPlayCounts.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        // Prepare result map
        Map<String, Object> result = new HashMap<>();
        result.put("track_id", trackId);
        result.put("title", track.getTitle());
        result.put("total_plays", track.getPlayCount());
        result.put("last_played", track.getLastPlayedAt());
        result.put("start_date", start.toString());
        result.put("end_date", end.toString());

        result.put("peak_day", getPeakDay(dailyPlayCounts));
        
        return result;
    }
    
    @Override
    public Map<String, Long> getWeeklyPlayCounts(int trackId, LocalDate startDate) {
        // Default to current week if not specified
        LocalDate firstDayOfWeek = Optional.ofNullable(startDate)
                .orElse(LocalDate.now())
                .with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()));
        
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);
        
        // Get daily play counts for the week
        Map<String, Long> dailyPlayCounts = getDailyPlayCounts(trackId, firstDayOfWeek, lastDayOfWeek);
        
        // Ensure all days of the week are included with at least 0 plays
        Map<String, Long> weeklyPlayCounts = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = firstDayOfWeek.plusDays(i);
            String dateStr = date.format(DATE_FORMATTER);
            weeklyPlayCounts.put(dateStr, dailyPlayCounts.getOrDefault(dateStr, 0L));
        }
        
        return weeklyPlayCounts;
    }
    
    @Override
    public Map<String, Long> getMonthlyPlayCounts(int trackId, Integer year) {
        int targetYear = Optional.ofNullable(year).orElse(LocalDate.now().getYear());
        
        // Create a map with all months initialized to 0 plays
        Map<String, Long> monthlyPlayCounts = IntStream.rangeClosed(1, 12)
                .boxed()
                .collect(Collectors.toMap(
                        month -> String.format("%04d-%02d", targetYear, month),
                        month -> 0L
                ));
        
        // This would be implemented with actual repository queries
        // to get monthly play counts from logs
        // For each month, query play logs and update the map
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(targetYear, month);
            String monthKey = String.format("%04d-%02d", targetYear, month);
            long playCount = getMonthlyPlayCount(trackId, yearMonth);
            monthlyPlayCounts.put(monthKey, playCount);
        }
        
        return monthlyPlayCounts;
    }
    
    @Override
    public Map<String, Object> getTrackEngagementStats(int trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));
        
        // This would involve complex queries to calculate:
        // - Unique listeners count
        // - Average listen duration
        // - Completion rate (how many people listen to the whole track)
        
        // For now, returning placeholder stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("track_id", trackId);
        stats.put("title", track.getTitle());
        stats.put("unique_listeners", getUniqueListeners(trackId));
        stats.put("avg_listen_duration", getAverageListenDuration(trackId));
        stats.put("completion_rate", getCompletionRate(trackId));
        stats.put("skip_rate", getSkipRate(trackId));
        
        return stats;
    }
    
    // Helper methods - these would involve actual database queries in a real implementation
    
    private Map<String, Long> getDailyPlayCounts(int trackId, LocalDate startDate, LocalDate endDate) {
        // This would query play logs to get actual daily counts
        // Placeholder implementation
        Map<String, Long> dailyCounts = new HashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(DATE_FORMATTER);
            // Simulated play count - in reality, query the database
            Long playCount = getDailyPlayCount(trackId, current);
            dailyCounts.put(dateStr, playCount);
            current = current.plusDays(1);
        }
        return dailyCounts;
    }
    
    private Long getDailyPlayCount(int trackId, LocalDate date) {
        // In a real implementation, query the database for play counts on this date
        // Placeholder implementation
        return 0L; // Would be replaced with actual query results
    }
    
    private String getPeakDay(Map<String, Long> dailyPlayCounts) {
        return dailyPlayCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No plays recorded");
    }
    
    private long getMonthlyPlayCount(int trackId, YearMonth yearMonth) {
        // In a real implementation, query the database for monthly play counts
        // Placeholder implementation
        return 0L; // Would be replaced with actual query results
    }
    
    private int getUniqueListeners(int trackId) {
        // Query to get count of unique users who played this track
        return 0; // Placeholder
    }
    
    private double getAverageListenDuration(int trackId) {
        // Query to get average time users spent listening to this track
        return 0.0; // Placeholder
    }
    
    private double getCompletionRate(int trackId) {
        // Calculate what percentage of plays completed the full track
        return 0.0; // Placeholder
    }
    
    private double getSkipRate(int trackId) {
        // Calculate what percentage of plays skipped the track before completion
        return 0.0; // Placeholder
    }
} 