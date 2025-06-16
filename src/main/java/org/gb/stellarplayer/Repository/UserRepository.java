package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // User search and pagination methods
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findUsersWithSearch(@Param("search") String search, Pageable pageable);
    
    // Find subscribed users ordered by longest subscription duration first
    @Query("SELECT DISTINCT u FROM User u " +
           "INNER JOIN UserSubscription us ON u.id = us.user.id " +
           "WHERE us.isActive = true AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY (SELECT MAX(DATEDIFF(COALESCE(us2.endDate, CURRENT_DATE), us2.startDate)) " +
           "FROM UserSubscription us2 WHERE us2.user.id = u.id) DESC")
    Page<User> findSubscribedUsersWithSearch(@Param("search") String search, Pageable pageable);
    
    // Get longest subscription duration for a user
    @Query("SELECT MAX(DATEDIFF(COALESCE(us.endDate, CURRENT_DATE), us.startDate)) " +
           "FROM UserSubscription us WHERE us.user.id = :userId")
    Integer findLongestSubscriptionDuration(@Param("userId") Integer userId);
    
    // Get current active subscription with plan details for a user
    @Query("SELECT us.id, us.startDate, us.endDate, us.isActive, " +
           "s.id, s.name, s.price, s.dateType " +
           "FROM UserSubscription us " +
           "INNER JOIN us.subscription s " +
           "WHERE us.user.id = :userId AND us.isActive = true " +
           "ORDER BY us.startDate DESC")
    List<Object[]> findCurrentSubscription(@Param("userId") Integer userId);
    
    // Get all subscriptions for a user (for longest subscription calculation)
    @Query("SELECT us.id, us.startDate, us.endDate, us.isActive, " +
           "s.id, s.name, s.price, s.dateType, " +
           "DATEDIFF(COALESCE(us.endDate, CURRENT_DATE), us.startDate) as duration_days " +
           "FROM UserSubscription us " +
           "INNER JOIN us.subscription s " +
           "WHERE us.user.id = :userId " +
           "ORDER BY DATEDIFF(COALESCE(us.endDate, CURRENT_DATE), us.startDate) DESC")
    List<Object[]> findAllUserSubscriptions(@Param("userId") Integer userId);
    
    // Update user enabled status
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.enabled = :enabled, u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    int updateUserEnabledStatus(@Param("userId") Integer userId, @Param("enabled") boolean enabled);
    
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
