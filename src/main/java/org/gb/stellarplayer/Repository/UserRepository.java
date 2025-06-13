package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String username);
    User findByEmail(String email);
    Boolean existsByName(String username);
    Boolean existsByEmail(String email);
    
    // Find users who have favorite artists (for artist radio refresh)
    @Query("SELECT DISTINCT u FROM User u JOIN UserFavouriteArtist ufa WHERE ufa.user = u")
    List<User> findUsersWithFavoriteArtists();
    
    // Find unverified users created before the specified cutoff time
    @Query("SELECT u.id FROM User u WHERE u.enabled = false AND u.createdAt < :cutoffTime")
    List<Integer> findUnverifiedUsersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Delete unverified users created before the specified cutoff time
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.enabled = false AND u.createdAt < :cutoffTime")
    int deleteUnverifiedUsersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}
