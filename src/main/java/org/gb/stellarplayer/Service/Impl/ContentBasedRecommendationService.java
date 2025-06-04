package org.gb.stellarplayer.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.gb.stellarplayer.DTO.RecommendationRequest;
import org.gb.stellarplayer.DTO.RecommendationResponse;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.Repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentBasedRecommendationService {

    private final TrackRepository trackRepository;
    private final UserTrackInteractionRepository userTrackInteractionRepository;
    private final UserRepository userRepository;

    /**
     * Generate content-based recommendations using TF-IDF and cosine similarity
     */
    public RecommendationResponse generateContentBasedRecommendations(RecommendationRequest request) {
        log.info("Generating content-based recommendations for user: {}", request.getUserId());

        List<Track> userLikedTracks = getUserLikedTracks(request.getUserId(), request.getMinInteractionScore());
        
        if (userLikedTracks.isEmpty()) {
            return getFallbackRecommendations(request);
        }

        // Create user profile based on liked tracks
        Map<String, Double> userProfile = createUserProfile(userLikedTracks);
        
        // Get all tracks for comparison
        List<Track> allTracks = trackRepository.findByStatusTrue();
        
        // Calculate similarity scores
        List<RecommendationResponse.RecommendedTrack> recommendations = allTracks.stream()
                .filter(track -> shouldIncludeTrack(track, request))
                .map(track -> {
                    double similarity = calculateContentSimilarity(userProfile, track);
                    return RecommendationResponse.RecommendedTrack.builder()
                            .track(track)
                            .recommendationScore(similarity)
                            .confidence(similarity * 0.8) // Content-based has 80% confidence
                            .reason("Based on your music preferences")
                            .tags(extractTrackTags(track))
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                .limit(request.getLimit())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("CONTENT_BASED")
                .totalRecommendations(recommendations.size())
                .averageConfidence(calculateAverageConfidence(recommendations))
                .algorithm("TF-IDF + Cosine Similarity")
                .build();
    }

    /**
     * Generate similar track recommendations
     */
    public RecommendationResponse generateSimilarTrackRecommendations(RecommendationRequest request) {
        log.info("Generating similar track recommendations for track: {}", request.getSeedTrackId());

        Optional<Track> seedTrackOpt = trackRepository.findById(request.getSeedTrackId());
        if (seedTrackOpt.isEmpty()) {
            return getFallbackRecommendations(request);
        }

        Track seedTrack = seedTrackOpt.get();
        Map<String, Double> seedProfile = createTrackProfile(seedTrack);

        List<Track> allTracks = trackRepository.findByStatusTrue();
        
        List<RecommendationResponse.RecommendedTrack> recommendations = allTracks.stream()
                .filter(track -> !track.getId().equals(request.getSeedTrackId()))
                .map(track -> {
                    double similarity = calculateContentSimilarity(seedProfile, track);
                    return RecommendationResponse.RecommendedTrack.builder()
                            .track(track)
                            .recommendationScore(similarity)
                            .confidence(similarity * 0.9) // Similar tracks have high confidence
                            .reason("Similar to " + seedTrack.getTitle())
                            .tags(extractTrackTags(track))
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                .limit(request.getLimit())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("SIMILAR_TRACKS")
                .totalRecommendations(recommendations.size())
                .averageConfidence(calculateAverageConfidence(recommendations))
                .algorithm("Content Similarity")
                .build();
    }

    private List<Track> getUserLikedTracks(Integer userId, Double minScore) {
        return userTrackInteractionRepository
                .findByUserIdAndInteractionScoreGreaterThanEqual(userId, minScore)
                .stream()
                .map(UserTrackInteraction::getTrack)
                .collect(Collectors.toList());
    }

    private Map<String, Double> createUserProfile(List<Track> likedTracks) {
        Map<String, Double> profile = new HashMap<>();
        
        for (Track track : likedTracks) {
            Map<String, Double> trackProfile = createTrackProfile(track);
            trackProfile.forEach((key, value) -> 
                profile.merge(key, value, Double::sum));
        }
        
        // Normalize the profile
        double total = profile.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            profile.replaceAll((key, value) -> value / total);
        }
        
        return profile;
    }

    private Map<String, Double> createTrackProfile(Track track) {
        Map<String, Double> profile = new HashMap<>();
        
        // Add genre features
        if (track.getGenres() != null) {
            for (Genre genre : track.getGenres()) {
                profile.put("genre_" + genre.getName().toLowerCase(), 1.0);
            }
        }
        
        // Add artist features
        if (track.getArtists() != null) {
            for (Artist artist : track.getArtists()) {
                profile.put("artist_" + artist.getName().toLowerCase(), 1.0);
            }
        }
        
        // Add duration category
        String durationCategory = categorizeDuration(track.getDuration());
        profile.put("duration_" + durationCategory, 1.0);
        
        // Add popularity score based on play count
        double popularityScore = Math.log(1 + (track.getPlayCount() != null ? track.getPlayCount() : 0));
        profile.put("popularity", popularityScore);
        
        return profile;
    }

    private double calculateContentSimilarity(Map<String, Double> profile1, Track track) {
        Map<String, Double> profile2 = createTrackProfile(track);
        return calculateCosineSimilarity(profile1, profile2);
    }

    private double calculateCosineSimilarity(Map<String, Double> profile1, Map<String, Double> profile2) {
        Set<String> allFeatures = new HashSet<>(profile1.keySet());
        allFeatures.addAll(profile2.keySet());
        
        if (allFeatures.isEmpty()) {
            return 0.0;
        }
        
        double[] vector1 = new double[allFeatures.size()];
        double[] vector2 = new double[allFeatures.size()];
        
        int i = 0;
        for (String feature : allFeatures) {
            vector1[i] = profile1.getOrDefault(feature, 0.0);
            vector2[i] = profile2.getOrDefault(feature, 0.0);
            i++;
        }
        
        RealVector v1 = new ArrayRealVector(vector1);
        RealVector v2 = new ArrayRealVector(vector2);
        
        double dotProduct = v1.dotProduct(v2);
        double norm1 = v1.getNorm();
        double norm2 = v2.getNorm();
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (norm1 * norm2);
    }

    private String categorizeDuration(int duration) {
        if (duration < 120) return "short";
        else if (duration < 300) return "medium";
        else return "long";
    }

    private boolean shouldIncludeTrack(Track track, RecommendationRequest request) {
        if (!request.getIncludeKnownTracks()) {
            // Check if user has already interacted with this track
            return userTrackInteractionRepository
                    .findByUserAndTrack(
                        userRepository.findById(request.getUserId()).orElse(null), 
                        track
                    ).isEmpty();
        }
        return true;
    }

    private List<String> extractTrackTags(Track track) {
        List<String> tags = new ArrayList<>();
        
        if (track.getGenres() != null) {
            tags.addAll(track.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList()));
        }
        
        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
            tags.add(track.getArtists().get(0).getName());
        }
        
        String durationCategory = categorizeDuration(track.getDuration());
        tags.add(durationCategory + " duration");
        
        return tags;
    }

    private RecommendationResponse getFallbackRecommendations(RecommendationRequest request) {
        // Return popular tracks as fallback
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