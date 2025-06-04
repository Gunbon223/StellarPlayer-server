package org.gb.stellarplayer.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gb.stellarplayer.DTO.RecommendationRequest;
import org.gb.stellarplayer.DTO.RecommendationResponse;
import org.gb.stellarplayer.Service.MusicRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class RecommendationController {

    private final MusicRecommendationService musicRecommendationService;

    /**
     * Get personalized music recommendations for a user
     */
    @PostMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        log.info("Received recommendation request for user: {} with type: {}", 
                request.getUserId(), request.getType());
        
        try {
            RecommendationResponse response = musicRecommendationService.getRecommendations(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating recommendations for user: {}", request.getUserId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get content-based recommendations
     */
    @GetMapping("/content-based/{userId}")
    public ResponseEntity<RecommendationResponse> getContentBasedRecommendations(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "2.0") Double minScore) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(userId)
                .type(RecommendationRequest.RecommendationType.CONTENT_BASED)
                .limit(limit)
                .minInteractionScore(minScore)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Get collaborative filtering recommendations
     */
    @GetMapping("/collaborative/{userId}")
    public ResponseEntity<RecommendationResponse> getCollaborativeRecommendations(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "2.0") Double minScore) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(userId)
                .type(RecommendationRequest.RecommendationType.COLLABORATIVE)
                .limit(limit)
                .minInteractionScore(minScore)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Get hybrid recommendations (default and recommended approach)
     */
    @GetMapping("/hybrid/{userId}")
    public ResponseEntity<RecommendationResponse> getHybridRecommendations(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "2.0") Double minScore,
            @RequestParam(defaultValue = "false") Boolean includeKnown) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(userId)
                .type(RecommendationRequest.RecommendationType.HYBRID)
                .limit(limit)
                .minInteractionScore(minScore)
                .includeKnownTracks(includeKnown)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Get similar tracks to a specific track
     */
    @GetMapping("/similar/{trackId}")
    public ResponseEntity<RecommendationResponse> getSimilarTracks(
            @PathVariable Integer trackId,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.SIMILAR_TRACKS)
                .seedTrackId(trackId)
                .limit(limit)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Get trending tracks
     */
    @GetMapping("/trending")
    public ResponseEntity<RecommendationResponse> getTrendingTracks(
            @RequestParam(defaultValue = "20") Integer limit) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.TRENDING)
                .limit(limit)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Get new releases
     */
    @GetMapping("/new-releases")
    public ResponseEntity<RecommendationResponse> getNewReleases(
            @RequestParam(defaultValue = "20") Integer limit) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.NEW_RELEASES)
                .limit(limit)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Get genre-based recommendations
     */
    @GetMapping("/genre")
    public ResponseEntity<RecommendationResponse> getGenreBasedRecommendations(
            @RequestParam List<Integer> genreIds,
            @RequestParam(defaultValue = "15") Integer limit) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.GENRE_BASED)
                .seedGenreIds(genreIds)
                .limit(limit)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Get artist-based recommendations
     */
    @GetMapping("/artist")
    public ResponseEntity<RecommendationResponse> getArtistBasedRecommendations(
            @RequestParam List<Integer> artistIds,
            @RequestParam(defaultValue = "15") Integer limit) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.ARTIST_BASED)
                .seedArtistIds(artistIds)
                .limit(limit)
                .build();
        
        return getRecommendations(request);
    }

    /**
     * Record user interaction with a track for learning
     */
    @PostMapping("/interaction")
    public ResponseEntity<Void> recordInteraction(
            @RequestParam Integer userId,
            @RequestParam Integer trackId,
            @RequestParam String interactionType,
            @RequestParam(required = false) Long listenTime) {
        
        try {
            musicRecommendationService.recordInteraction(userId, trackId, interactionType, listenTime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error recording interaction for user: {} and track: {}", userId, trackId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get personalized recommendations with all options
     */
    @GetMapping("/personalized/{userId}")
    public ResponseEntity<RecommendationResponse> getPersonalizedRecommendations(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "HYBRID") String type,
            @RequestParam(defaultValue = "15") Integer limit,
            @RequestParam(defaultValue = "2.0") Double minScore,
            @RequestParam(defaultValue = "0.3") Double diversityFactor,
            @RequestParam(defaultValue = "false") Boolean includeKnown) {
        
        RecommendationRequest.RecommendationType recommendationType;
        try {
            recommendationType = RecommendationRequest.RecommendationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            recommendationType = RecommendationRequest.RecommendationType.HYBRID;
        }
        
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(userId)
                .type(recommendationType)
                .limit(limit)
                .minInteractionScore(minScore)
                .diversityFactor(diversityFactor)
                .includeKnownTracks(includeKnown)
                .build();
        
        return getRecommendations(request);
    }
} 