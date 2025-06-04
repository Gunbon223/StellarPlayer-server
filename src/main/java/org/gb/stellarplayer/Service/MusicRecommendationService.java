package org.gb.stellarplayer.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gb.stellarplayer.DTO.RecommendationRequest;
import org.gb.stellarplayer.DTO.RecommendationResponse;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserTrackInteraction;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Repository.UserTrackInteractionRepository;
import org.gb.stellarplayer.Service.Impl.CollaborativeFilteringService;
import org.gb.stellarplayer.Service.Impl.ContentBasedRecommendationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicRecommendationService {

    private final ContentBasedRecommendationService contentBasedService;
    private final CollaborativeFilteringService collaborativeService;
    private final UserTrackInteractionRepository userTrackInteractionRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    /**
     * Get personalized music recommendations for a user
     */
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        log.info("Getting recommendations for user: {} with type: {}", 
                request.getUserId(), request.getType());

        switch (request.getType()) {
            case CONTENT_BASED:
                return contentBasedService.generateContentBasedRecommendations(request);
            case COLLABORATIVE:
                return collaborativeService.generateCollaborativeRecommendations(request);
            case HYBRID:
                return generateHybridRecommendations(request);
            case SIMILAR_TRACKS:
                return contentBasedService.generateSimilarTrackRecommendations(request);
            case TRENDING:
                return generateTrendingRecommendations(request);
            case NEW_RELEASES:
                return generateNewReleasesRecommendations(request);
            case GENRE_BASED:
                return generateGenreBasedRecommendations(request);
            case ARTIST_BASED:
                return generateArtistBasedRecommendations(request);
            default:
                return generateHybridRecommendations(request);
        }
    }

    /**
     * Generate hybrid recommendations combining content-based and collaborative filtering
     */
    private RecommendationResponse generateHybridRecommendations(RecommendationRequest request) {
        log.info("Generating hybrid recommendations for user: {}", request.getUserId());

        // Get recommendations from both algorithms
        RecommendationResponse contentBased = contentBasedService
                .generateContentBasedRecommendations(request);
        RecommendationResponse collaborative = collaborativeService
                .generateCollaborativeRecommendations(request);

        // Combine and weight the recommendations
        Map<Integer, RecommendationResponse.RecommendedTrack> combinedTracks = new HashMap<>();

        // Weight: 60% collaborative, 40% content-based
        double collaborativeWeight = 0.6;
        double contentWeight = 0.4;

        // Add collaborative recommendations
        for (RecommendationResponse.RecommendedTrack track : collaborative.getRecommendedTracks()) {
            track.setRecommendationScore(track.getRecommendationScore() * collaborativeWeight);
            track.setConfidence(track.getConfidence() * collaborativeWeight);
            combinedTracks.put(track.getTrack().getId(), track);
        }

        // Add content-based recommendations (combining scores if track exists)
        for (RecommendationResponse.RecommendedTrack track : contentBased.getRecommendedTracks()) {
            Integer trackId = track.getTrack().getId();
            double weightedScore = track.getRecommendationScore() * contentWeight;
            double weightedConfidence = track.getConfidence() * contentWeight;

            if (combinedTracks.containsKey(trackId)) {
                RecommendationResponse.RecommendedTrack existing = combinedTracks.get(trackId);
                existing.setRecommendationScore(existing.getRecommendationScore() + weightedScore);
                existing.setConfidence(existing.getConfidence() + weightedConfidence);
                existing.setReason("Based on similar users and your preferences");
            } else {
                track.setRecommendationScore(weightedScore);
                track.setConfidence(weightedConfidence);
                combinedTracks.put(trackId, track);
            }
        }

        // Sort and limit results
        List<RecommendationResponse.RecommendedTrack> finalRecommendations = combinedTracks.values().stream()
                .sorted((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                .limit(request.getLimit())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(finalRecommendations)
                .recommendationType("HYBRID")
                .totalRecommendations(finalRecommendations.size())
                .averageConfidence(calculateAverageConfidence(finalRecommendations))
                .algorithm("Hybrid (Collaborative + Content-Based)")
                .build();
    }

    /**
     * Generate trending recommendations based on recent popularity
     */
    private RecommendationResponse generateTrendingRecommendations(RecommendationRequest request) {
        log.info("Generating trending recommendations");

        LocalDateTime since = LocalDateTime.now().minusDays(7); // Last 7 days
        List<Object[]> trendingData = userTrackInteractionRepository
                .findTrendingTracks(since, PageRequest.of(0, request.getLimit()));

        List<RecommendationResponse.RecommendedTrack> recommendations = trendingData.stream()
                .map(data -> {
                    Integer trackId = (Integer) data[0];
                    Long playCount = (Long) data[1];
                    
                    Track track = trackRepository.findById(trackId).orElse(null);
                    if (track == null) return null;

                    double score = Math.log(1 + playCount) / 10.0; // Normalize score
                    
                    return RecommendationResponse.RecommendedTrack.builder()
                            .track(track)
                            .recommendationScore(score)
                            .confidence(0.8)
                            .reason("Trending this week")
                            .tags(Arrays.asList("trending", "popular"))
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("TRENDING")
                .totalRecommendations(recommendations.size())
                .averageConfidence(0.8)
                .algorithm("Trending Analysis")
                .build();
    }

    /**
     * Generate new releases recommendations
     */
    private RecommendationResponse generateNewReleasesRecommendations(RecommendationRequest request) {
        log.info("Generating new releases recommendations");

        LocalDateTime since = LocalDateTime.now().minusDays(30); // Last 30 days
        List<Track> newTracks = trackRepository.findByStatusTrueAndCreatedAtAfterOrderByCreatedAtDesc(since)
                .stream()
                .limit(request.getLimit())
                .collect(Collectors.toList());

        List<RecommendationResponse.RecommendedTrack> recommendations = newTracks.stream()
                .map(track -> RecommendationResponse.RecommendedTrack.builder()
                        .track(track)
                        .recommendationScore(0.7)
                        .confidence(0.6)
                        .reason("New release")
                        .tags(Arrays.asList("new", "latest"))
                        .build())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("NEW_RELEASES")
                .totalRecommendations(recommendations.size())
                .averageConfidence(0.6)
                .algorithm("New Releases")
                .build();
    }

    /**
     * Generate genre-based recommendations
     */
    private RecommendationResponse generateGenreBasedRecommendations(RecommendationRequest request) {
        log.info("Generating genre-based recommendations for genres: {}", request.getSeedGenreIds());

        if (request.getSeedGenreIds() == null || request.getSeedGenreIds().isEmpty()) {
            return getFallbackRecommendations(request);
        }

        List<Track> genreTracks = trackRepository.findByGenreIdsAndStatusTrue(request.getSeedGenreIds())
                .stream()
                .limit(request.getLimit())
                .collect(Collectors.toList());

        List<RecommendationResponse.RecommendedTrack> recommendations = genreTracks.stream()
                .map(track -> RecommendationResponse.RecommendedTrack.builder()
                        .track(track)
                        .recommendationScore(0.8)
                        .confidence(0.9)
                        .reason("Matches your preferred genres")
                        .tags(extractTrackTags(track))
                        .build())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("GENRE_BASED")
                .totalRecommendations(recommendations.size())
                .averageConfidence(0.9)
                .algorithm("Genre Matching")
                .build();
    }

    /**
     * Generate artist-based recommendations
     */
    private RecommendationResponse generateArtistBasedRecommendations(RecommendationRequest request) {
        log.info("Generating artist-based recommendations for artists: {}", request.getSeedArtistIds());

        if (request.getSeedArtistIds() == null || request.getSeedArtistIds().isEmpty()) {
            return getFallbackRecommendations(request);
        }

        List<Track> artistTracks = trackRepository.findByArtistIdsAndStatusTrue(request.getSeedArtistIds())
                .stream()
                .limit(request.getLimit())
                .collect(Collectors.toList());

        List<RecommendationResponse.RecommendedTrack> recommendations = artistTracks.stream()
                .map(track -> RecommendationResponse.RecommendedTrack.builder()
                        .track(track)
                        .recommendationScore(0.9)
                        .confidence(0.95)
                        .reason("From your favorite artists")
                        .tags(extractTrackTags(track))
                        .build())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("ARTIST_BASED")
                .totalRecommendations(recommendations.size())
                .averageConfidence(0.95)
                .algorithm("Artist Matching")
                .build();
    }

    /**
     * Record user interaction with a track for recommendation learning
     */
    @Transactional
    public void recordInteraction(Integer userId, Integer trackId, String interactionType, Long listenTime) {
        log.info("Recording interaction: user={}, track={}, type={}, time={}", 
                userId, trackId, interactionType, listenTime);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        UserTrackInteraction interaction = userTrackInteractionRepository
                .findByUserAndTrack(user, track)
                .orElse(UserTrackInteraction.builder()
                        .user(user)
                        .track(track)
                        .build());

        // Update interaction based on type
        switch (interactionType.toLowerCase()) {
            case "play":
                interaction.setPlayCount(interaction.getPlayCount() + 1);
                if (listenTime != null) {
                    interaction.setTotalListenTime(interaction.getTotalListenTime() + listenTime);
                }
                break;
            case "skip":
                interaction.setSkipCount(interaction.getSkipCount() + 1);
                break;
            case "like":
                interaction.setIsLiked(true);
                break;
            case "unlike":
                interaction.setIsLiked(false);
                break;
            case "share":
                interaction.setIsShared(true);
                break;
        }

        // Recalculate interaction score
        interaction.calculateInteractionScore(track.getDuration());
        
        userTrackInteractionRepository.save(interaction);
    }

    private List<String> extractTrackTags(Track track) {
        List<String> tags = new ArrayList<>();
        
        if (track.getGenres() != null) {
            tags.addAll(track.getGenres().stream()
                    .map(genre -> genre.getName())
                    .collect(Collectors.toList()));
        }
        
        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
            tags.add(track.getArtists().get(0).getName());
        }
        
        return tags;
    }

    private RecommendationResponse getFallbackRecommendations(RecommendationRequest request) {
        List<Track> popularTracks = trackRepository.findByStatusTrueOrderByPlayCountDesc()
                .stream()
                .limit(request.getLimit())
                .collect(Collectors.toList());

        List<RecommendationResponse.RecommendedTrack> recommendations = popularTracks.stream()
                .map(track -> RecommendationResponse.RecommendedTrack.builder()
                        .track(track)
                        .recommendationScore(0.5)
                        .confidence(0.3)
                        .reason("Popular track")
                        .tags(extractTrackTags(track))
                        .build())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("FALLBACK")
                .totalRecommendations(recommendations.size())
                .averageConfidence(0.3)
                .algorithm("Popularity-based")
                .build();
    }

    private Double calculateAverageConfidence(List<RecommendationResponse.RecommendedTrack> recommendations) {
        return recommendations.stream()
                .mapToDouble(RecommendationResponse.RecommendedTrack::getConfidence)
                .average()
                .orElse(0.0);
    }
} 