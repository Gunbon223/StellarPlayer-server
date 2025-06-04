package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;
    
    // Number of recommendations to return
    @Builder.Default
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    private Integer limit = 10;
    
    // Recommendation type
    @Builder.Default
    private RecommendationType type = RecommendationType.HYBRID;
    
    // Specific track ID for similar track recommendations
    private Integer seedTrackId;
    
    // Specific artist IDs for artist-based recommendations
    private List<Integer> seedArtistIds;
    
    // Specific genre IDs for genre-based recommendations
    private List<Integer> seedGenreIds;
    
    // Minimum interaction score threshold
    @Builder.Default
    private Double minInteractionScore = 2.0;
    
    // Include tracks user has already interacted with
    @Builder.Default
    private Boolean includeKnownTracks = false;
    
    // Diversity factor (0.0 - 1.0, higher means more diverse)
    @Builder.Default
    private Double diversityFactor = 0.3;
    
    public enum RecommendationType {
        CONTENT_BASED,      // Based on track features
        COLLABORATIVE,      // Based on similar users
        HYBRID,            // Combination of both
        SIMILAR_TRACKS,    // Similar to a specific track
        TRENDING,          // Popular tracks
        NEW_RELEASES,      // Recently added tracks
        GENRE_BASED,       // Based on preferred genres
        ARTIST_BASED       // Based on preferred artists
    }
} 