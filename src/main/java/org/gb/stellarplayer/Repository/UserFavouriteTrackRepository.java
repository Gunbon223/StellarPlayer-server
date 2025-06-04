package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.UserFavouriteTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavouriteTrackRepository extends JpaRepository<UserFavouriteTrack, Integer> {
    List<UserFavouriteTrack> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<UserFavouriteTrack> findByUserIdAndTrackId(Integer userId, Integer trackId);
    boolean existsByUserIdAndTrackId(Integer userId, Integer trackId);
    void deleteByUserIdAndTrackId(Integer userId, Integer trackId);
    
    @Query("SELECT COUNT(f) FROM UserFavouriteTrack f WHERE f.track.id = :trackId")
    long countByTrackId(@Param("trackId") Integer trackId);
    
    @Query("SELECT f FROM UserFavouriteTrack f JOIN FETCH f.track WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<UserFavouriteTrack> findByUserIdWithTrackOrderByCreatedAtDesc(@Param("userId") Integer userId);
    
    // Paginated methods
    Page<UserFavouriteTrack> findByUserId(Integer userId, Pageable pageable);
    
    @Query("SELECT f FROM UserFavouriteTrack f JOIN FETCH f.track WHERE f.user.id = :userId")
    Page<UserFavouriteTrack> findByUserIdWithTrack(@Param("userId") Integer userId, Pageable pageable);
} 