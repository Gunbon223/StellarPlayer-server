package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.TrackPlayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackPlayLogRepository extends JpaRepository<TrackPlayLog, Long> {
    
    Optional<TrackPlayLog> findByTrackIdAndIpAddress(Integer trackId, String ipAddress);
    
    @Query("SELECT t FROM TrackPlayLog t WHERE t.track.id = :trackId AND t.ipAddress = :ipAddress AND t.lastPlayedAt >= :timeLimit")
    List<TrackPlayLog> findRecentPlaysByTrackAndIp(
        @Param("trackId") Integer trackId,
        @Param("ipAddress") String ipAddress,
        @Param("timeLimit") LocalDateTime timeLimit
    );
    
    @Query("SELECT COUNT(t) FROM TrackPlayLog t WHERE t.track.id = :trackId AND t.ipAddress = :ipAddress AND t.lastPlayedAt >= :startOfDay")
    Integer countDailyPlaysByTrackAndIp(
        @Param("trackId") Integer trackId,
        @Param("ipAddress") String ipAddress,
        @Param("startOfDay") LocalDateTime startOfDay
    );

    // Find play logs for a track in a date range
    List<TrackPlayLog> findByTrackIdAndLastPlayedAtBetween(Integer trackId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Count unique listeners for a track
    @Query("SELECT COUNT(DISTINCT t.ipAddress) FROM TrackPlayLog t WHERE t.track.id = ?1")
    Integer countUniqueListeners(Integer trackId);
    
    // Get average listen duration (this would be a complex query in reality)
    @Query("SELECT AVG(t.playCount) FROM TrackPlayLog t WHERE t.track.id = ?1")
    Double getAveragePlayCount(Integer trackId);
} 