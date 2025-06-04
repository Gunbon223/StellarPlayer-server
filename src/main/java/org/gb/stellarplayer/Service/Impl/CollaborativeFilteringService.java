package org.gb.stellarplayer.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.gb.stellarplayer.DTO.RecommendationRequest;
import org.gb.stellarplayer.DTO.RecommendationResponse;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserTrackInteraction;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Repository.UserTrackInteractionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborativeFilteringService {

    private final UserTrackInteractionRepository userTrackInteractionRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

    /**
     * Generate collaborative filtering recommendations using user-item matrix
     */
    public RecommendationResponse generateCollaborativeRecommendations(RecommendationRequest request) {
        log.info("Generating collaborative filtering recommendations for user: {}", request.getUserId());

        // Get user's interaction history
        List<UserTrackInteraction> userInteractions = userTrackInteractionRepository
                .findByUserIdAndInteractionScoreGreaterThanEqual(request.getUserId(), request.getMinInteractionScore());

        if (userInteractions.isEmpty()) {
            return getFallbackRecommendations(request);
        }

        // Find similar users based on interaction patterns
        List<SimilarUser> similarUsers = findSimilarUsers(request.getUserId(), request.getMinInteractionScore());

        if (similarUsers.isEmpty()) {
            return getFallbackRecommendations(request);
        }

        // Get recommendations from similar users
        Map<Integer, Double> trackScores = calculateTrackScoresFromSimilarUsers(
                similarUsers, request.getUserId(), userInteractions);

        // Convert to recommendation response
        List<RecommendationResponse.RecommendedTrack> recommendations = trackScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(request.getLimit())
                .map(entry -> {
                    Track track = trackRepository.findById(entry.getKey()).orElse(null);
                    if (track == null) return null;
                    
                    return RecommendationResponse.RecommendedTrack.builder()
                            .track(track)
                            .recommendationScore(entry.getValue())
                            .confidence(entry.getValue() * 0.7) // Collaborative filtering confidence
                            .reason("Users with similar taste also liked this")
                            .tags(extractTrackTags(track))
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("COLLABORATIVE")
                .totalRecommendations(recommendations.size())
                .averageConfidence(calculateAverageConfidence(recommendations))
                .algorithm("User-Based Collaborative Filtering")
                .build();
    }

    /**
     * Find users similar to the target user based on interaction patterns
     */
    private List<SimilarUser> findSimilarUsers(Integer targetUserId, Double minScore) {
        // Get all users who have similar interactions
        List<UserTrackInteraction> similarUserInteractions = userTrackInteractionRepository
                .findSimilarUsersInteractions(targetUserId, minScore);

        // Group by user and calculate similarity scores
        Map<Integer, List<UserTrackInteraction>> interactionsByUser = similarUserInteractions.stream()
                .collect(Collectors.groupingBy(interaction -> interaction.getUser().getId()));

        // Get target user's interactions for comparison
        List<UserTrackInteraction> targetUserInteractions = userTrackInteractionRepository
                .findByUserIdAndInteractionScoreGreaterThanEqual(targetUserId, minScore);

        Map<Integer, Double> targetUserVector = createUserVector(targetUserInteractions);

        return interactionsByUser.entrySet().stream()
                .map(entry -> {
                    Integer userId = entry.getKey();
                    List<UserTrackInteraction> interactions = entry.getValue();
                    
                    Map<Integer, Double> userVector = createUserVector(interactions);
                    double similarity = calculateUserSimilarity(targetUserVector, userVector);
                    
                    return new SimilarUser(userId, similarity);
                })
                .filter(similarUser -> similarUser.similarity > 0.1) // Minimum similarity threshold
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                .limit(50) // Top 50 similar users
                .collect(Collectors.toList());
    }

    /**
     * Create a user vector from their interactions (track_id -> interaction_score)
     */
    private Map<Integer, Double> createUserVector(List<UserTrackInteraction> interactions) {
        return interactions.stream()
                .collect(Collectors.toMap(
                        interaction -> interaction.getTrack().getId(),
                        UserTrackInteraction::getInteractionScore,
                        Double::max // In case of duplicates, take max score
                ));
    }

    /**
     * Calculate similarity between two users using cosine similarity
     */
    private double calculateUserSimilarity(Map<Integer, Double> vector1, Map<Integer, Double> vector2) {
        Set<Integer> commonTracks = new HashSet<>(vector1.keySet());
        commonTracks.retainAll(vector2.keySet());

        if (commonTracks.isEmpty()) {
            return 0.0;
        }

        // Calculate cosine similarity for common tracks
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Integer trackId : commonTracks) {
            double score1 = vector1.get(trackId);
            double score2 = vector2.get(trackId);
            
            dotProduct += score1 * score2;
            norm1 += score1 * score1;
            norm2 += score2 * score2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        
        // Apply Jaccard coefficient to penalize users with few common tracks
        double jaccardCoeff = (double) commonTracks.size() / 
                (vector1.size() + vector2.size() - commonTracks.size());
        
        return similarity * Math.sqrt(jaccardCoeff);
    }

    /**
     * Calculate track scores based on similar users' preferences
     */
    private Map<Integer, Double> calculateTrackScoresFromSimilarUsers(
            List<SimilarUser> similarUsers, Integer targetUserId, List<UserTrackInteraction> targetUserInteractions) {
        
        Set<Integer> targetUserTrackIds = targetUserInteractions.stream()
                .map(interaction -> interaction.getTrack().getId())
                .collect(Collectors.toSet());

        Map<Integer, Double> trackScores = new HashMap<>();

        for (SimilarUser similarUser : similarUsers) {
            List<UserTrackInteraction> userInteractions = userTrackInteractionRepository
                    .findByUserIdOrderByInteractionScoreDesc(similarUser.userId);

            for (UserTrackInteraction interaction : userInteractions) {
                Integer trackId = interaction.getTrack().getId();
                
                // Skip tracks the target user has already interacted with
                if (targetUserTrackIds.contains(trackId)) {
                    continue;
                }

                // Weight the interaction score by user similarity
                double weightedScore = interaction.getInteractionScore() * similarUser.similarity;
                trackScores.merge(trackId, weightedScore, Double::sum);
            }
        }

        return trackScores;
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
        // Return trending tracks as fallback
        List<Track> trendingTracks = trackRepository.findByStatusTrueOrderByPlayCountDesc()
                .stream()
                .limit(request.getLimit())
                .collect(Collectors.toList());

        List<RecommendationResponse.RecommendedTrack> recommendations = trendingTracks.stream()
                .map(track -> RecommendationResponse.RecommendedTrack.builder()
                        .track(track)
                        .recommendationScore(0.4)
                        .confidence(0.2)
                        .reason("Trending track")
                        .tags(extractTrackTags(track))
                        .build())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .recommendedTracks(recommendations)
                .recommendationType("FALLBACK_TRENDING")
                .totalRecommendations(recommendations.size())
                .averageConfidence(0.2)
                .algorithm("Popularity-based")
                .build();
    }

    private Double calculateAverageConfidence(List<RecommendationResponse.RecommendedTrack> recommendations) {
        return recommendations.stream()
                .mapToDouble(RecommendationResponse.RecommendedTrack::getConfidence)
                .average()
                .orElse(0.0);
    }

    /**
     * Helper class to store similar user information
     */
    private static class SimilarUser {
        final Integer userId;
        final Double similarity;

        SimilarUser(Integer userId, Double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }
    }
} 