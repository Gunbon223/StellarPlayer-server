package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.Track;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private List<RecommendedTrack> recommendedTracks;
    private String recommendationType;
    private Integer totalRecommendations;
    private Double averageConfidence;
    private String algorithm;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedTrack {
        private Track track;
        private Double recommendationScore;
        private Double confidence;
        private String reason; // Why this track was recommended
        private List<String> tags; // Genre, mood, etc.
    }
} 