package org.gb.stellarplayer.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stellarplayer.recommendation")
@Data
public class RecommendationConfig {
    
    /**
     * Hybrid algorithm weights
     */
    private Hybrid hybrid = new Hybrid();
    
    /**
     * Content-based filtering settings
     */
    private ContentBased contentBased = new ContentBased();
    
    /**
     * Collaborative filtering settings
     */
    private Collaborative collaborative = new Collaborative();
    
    /**
     * General recommendation settings
     */
    private General general = new General();
    
    @Data
    public static class Hybrid {
        private double collaborativeWeight = 0.6;
        private double contentBasedWeight = 0.4;
    }
    
    @Data
    public static class ContentBased {
        private double confidenceMultiplier = 0.8;
        private double similarityThreshold = 0.1;
        private int shortDurationThreshold = 120; // seconds
        private int longDurationThreshold = 300; // seconds
    }
    
    @Data
    public static class Collaborative {
        private double confidenceMultiplier = 0.7;
        private double similarityThreshold = 0.1;
        private int maxSimilarUsers = 50;
        private double minCommonTracks = 3;
    }
    
    @Data
    public static class General {
        private double defaultMinInteractionScore = 2.0;
        private int defaultLimit = 10;
        private int maxLimit = 100;
        private double defaultDiversityFactor = 0.3;
        private int trendingDays = 7;
        private int newReleasesDays = 30;
        private double fallbackConfidence = 0.3;
    }
} 