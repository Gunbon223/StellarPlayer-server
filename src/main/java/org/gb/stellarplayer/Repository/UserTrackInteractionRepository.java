package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.UserTrackInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTrackInteractionRepository extends JpaRepository<UserTrackInteraction, Long> {
    
    Optional<UserTrackInteraction> findByUserAndTrack(User user, Track track);
    
    List<UserTrackInteraction> findByUserIdOrderByInteractionScoreDesc(Integer userId);
    
    List<UserTrackInteraction> findByUserIdAndInteractionScoreGreaterThanEqual(
        Integer userId, Double minScore);
    
    @Query("SELECT uti FROM UserTrackInteraction uti " +
           "WHERE uti.user.id = :userId " +
           "AND uti.interactionScore >= :minScore " +
           "ORDER BY uti.interactionScore DESC, uti.lastInteractionAt DESC")
    Page<UserTrackInteraction> findTopInteractionsByUser(
        @Param("userId") Integer userId, 
        @Param("minScore") Double minScore, 
        Pageable pageable);
    
    @Query("SELECT uti.track.id, AVG(uti.interactionScore) " +
           "FROM UserTrackInteraction uti " +
           "WHERE uti.interactionScore >= :minScore " +
           "GROUP BY uti.track.id " +
           "ORDER BY AVG(uti.interactionScore) DESC")
    List<Object[]> findAverageInteractionScoreByTrack(@Param("minScore") Double minScore);
    
    @Query("SELECT uti FROM UserTrackInteraction uti " +
           "WHERE uti.user.id IN (" +
           "  SELECT DISTINCT uti2.user.id FROM UserTrackInteraction uti2 " +
           "  WHERE uti2.track.id IN (" +
           "    SELECT uti3.track.id FROM UserTrackInteraction uti3 " +
           "    WHERE uti3.user.id = :userId AND uti3.interactionScore >= :minScore" +
           "  ) AND uti2.user.id != :userId AND uti2.interactionScore >= :minScore" +
           ") " +
           "AND uti.track.id NOT IN (" +
           "  SELECT uti4.track.id FROM UserTrackInteraction uti4 " +
           "  WHERE uti4.user.id = :userId" +
           ") " +
           "ORDER BY uti.interactionScore DESC")
    List<UserTrackInteraction> findSimilarUsersInteractions(
        @Param("userId") Integer userId, 
        @Param("minScore") Double minScore);
    
    @Query("SELECT COUNT(DISTINCT uti.user.id) FROM UserTrackInteraction uti " +
           "WHERE uti.track.id = :trackId AND uti.interactionScore >= :minScore")
    Long countUsersWhoLikedTrack(@Param("trackId") Integer trackId, @Param("minScore") Double minScore);
    
    @Query("SELECT uti FROM UserTrackInteraction uti " +
           "WHERE uti.lastInteractionAt >= :since " +
           "ORDER BY uti.interactionScore DESC, uti.lastInteractionAt DESC")
    List<UserTrackInteraction> findRecentInteractions(
        @Param("since") LocalDateTime since, 
        Pageable pageable);
    
    @Query("SELECT uti.track.id, COUNT(uti) as playCount " +
           "FROM UserTrackInteraction uti " +
           "WHERE uti.lastInteractionAt >= :since " +
           "GROUP BY uti.track.id " +
           "ORDER BY playCount DESC")
    List<Object[]> findTrendingTracks(@Param("since") LocalDateTime since, Pageable pageable);
    
    // For recommendation system
    @Query("SELECT uti FROM UserTrackInteraction uti " +
           "WHERE uti.user.id = :userId " +
           "ORDER BY uti.interactionScore DESC")
    List<UserTrackInteraction> findTopByUserIdOrderByInteractionScoreDesc(@Param("userId") Integer userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM UserTrackInteraction uti WHERE uti.track.id = :trackId")
    void deleteByTrackId(@Param("trackId") Integer trackId);
} 