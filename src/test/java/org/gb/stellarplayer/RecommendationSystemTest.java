package org.gb.stellarplayer;

import org.gb.stellarplayer.DTO.RecommendationRequest;
import org.gb.stellarplayer.DTO.RecommendationResponse;
import org.gb.stellarplayer.Service.MusicRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RecommendationSystemTest {

    @Autowired
    private MusicRecommendationService recommendationService;

    @Test
    public void testHybridRecommendations() {
        // Test hybrid recommendation algorithm
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(1)
                .type(RecommendationRequest.RecommendationType.HYBRID)
                .limit(10)
                .minInteractionScore(1.0)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        assertEquals("HYBRID", response.getRecommendationType());
        assertTrue(response.getTotalRecommendations() >= 0);
        assertNotNull(response.getRecommendedTracks());
        assertEquals("Hybrid (Collaborative + Content-Based)", response.getAlgorithm());
    }

    @Test
    public void testContentBasedRecommendations() {
        // Test content-based recommendation algorithm
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(1)
                .type(RecommendationRequest.RecommendationType.CONTENT_BASED)
                .limit(5)
                .minInteractionScore(1.0)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        assertEquals("CONTENT_BASED", response.getRecommendationType());
        assertTrue(response.getTotalRecommendations() >= 0);
        assertNotNull(response.getRecommendedTracks());
        assertEquals("TF-IDF + Cosine Similarity", response.getAlgorithm());
    }

    @Test
    public void testCollaborativeFilteringRecommendations() {
        // Test collaborative filtering algorithm
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(1)
                .type(RecommendationRequest.RecommendationType.COLLABORATIVE)
                .limit(8)
                .minInteractionScore(1.0)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        assertEquals("COLLABORATIVE", response.getRecommendationType());
        assertTrue(response.getTotalRecommendations() >= 0);
        assertNotNull(response.getRecommendedTracks());
        assertEquals("User-Based Collaborative Filtering", response.getAlgorithm());
    }

    @Test
    public void testTrendingRecommendations() {
        // Test trending tracks algorithm
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.TRENDING)
                .limit(15)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        assertEquals("TRENDING", response.getRecommendationType());
        assertTrue(response.getTotalRecommendations() >= 0);
        assertNotNull(response.getRecommendedTracks());
        assertEquals("Trending Analysis", response.getAlgorithm());
    }

    @Test
    public void testNewReleasesRecommendations() {
        // Test new releases algorithm
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.NEW_RELEASES)
                .limit(12)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        assertEquals("NEW_RELEASES", response.getRecommendationType());
        assertTrue(response.getTotalRecommendations() >= 0);
        assertNotNull(response.getRecommendedTracks());
        assertEquals("New Releases", response.getAlgorithm());
    }

    @Test
    public void testInteractionRecording() {
        // Test interaction recording functionality
        Integer userId = 1;
        Integer trackId = 1;
        String interactionType = "play";
        Long listenTime = 120L;

        // This should not throw any exception
        assertDoesNotThrow(() -> {
            recommendationService.recordInteraction(userId, trackId, interactionType, listenTime);
        });
    }

    @Test
    public void testRecommendationScoring() {
        // Test that recommendations have proper scoring
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(1)
                .type(RecommendationRequest.RecommendationType.HYBRID)
                .limit(5)
                .minInteractionScore(1.0)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        if (!response.getRecommendedTracks().isEmpty()) {
            for (RecommendationResponse.RecommendedTrack track : response.getRecommendedTracks()) {
                // Check that each recommendation has valid scoring
                assertNotNull(track.getRecommendationScore());
                assertNotNull(track.getConfidence());
                assertNotNull(track.getReason());
                assertNotNull(track.getTags());
                assertTrue(track.getRecommendationScore() >= 0.0);
                assertTrue(track.getConfidence() >= 0.0);
                assertTrue(track.getConfidence() <= 1.0);
            }
        }
    }

    @Test
    public void testRecommendationLimits() {
        // Test that limit parameter is respected
        int requestedLimit = 3;
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(1)
                .type(RecommendationRequest.RecommendationType.HYBRID)
                .limit(requestedLimit)
                .minInteractionScore(1.0)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        assertTrue(response.getRecommendedTracks().size() <= requestedLimit);
    }

    @Test
    public void testFallbackRecommendations() {
        // Test fallback for non-existent user
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(999999) // Non-existent user
                .type(RecommendationRequest.RecommendationType.CONTENT_BASED)
                .limit(5)
                .minInteractionScore(1.0)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        // Should provide fallback recommendations
        assertNotNull(response.getRecommendedTracks());
    }
} 