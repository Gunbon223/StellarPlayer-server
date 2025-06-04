package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.Track;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackStatsDTO {
    private Integer id;
    private String title;
    private Long playCount;
    private LocalDateTime lastPlayedAt;
    private Integer dailyPlayCount;
    private LocalDateTime lastDailyReset;
    private Boolean isSuspicious;
    
    // For time-based statistics
    private Map<String, Long> playCountByDay;
    private Map<String, Long> playCountByWeek;
    private Map<String, Long> playCountByMonth;
    
    // Growth metrics
    private Double playCountGrowthRate;
    private Integer playCountLastWeek;
    private Integer playCountThisWeek;
    
    // For user engagement
    private Integer uniqueListeners;
    private Double averageListenDuration;
    private Double completionRate;
    
    public static TrackStatsDTO basicStatsFromTrack(Track track) {
        return TrackStatsDTO.builder()
                .id(track.getId())
                .title(track.getTitle())
                .playCount(track.getPlayCount())
                .lastPlayedAt(track.getLastPlayedAt())
                .dailyPlayCount(track.getDailyPlayCount())
                .lastDailyReset(track.getLastDailyReset())
                .isSuspicious(track.getIsSuspicious())
                .build();
    }
} 