package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    // Find all subscriptions for a user
    List<UserSubscription> findByUser(User user);

    // Find active subscriptions for a user
    List<UserSubscription> findByUserAndIsActiveTrue(User user);

    // Find all active subscriptions
    List<UserSubscription> findByIsActiveTrue();

    // Find the most recent active subscription for a user
    @Query("SELECT us FROM UserSubscription us WHERE us.user = ?1 AND us.isActive = true ORDER BY us.endDate DESC")
    List<UserSubscription> findActiveSubscriptionsByUserOrderByEndDateDesc(User user);

    // Find the most recent subscription for a user regardless of active status
    @Query("SELECT us FROM UserSubscription us WHERE us.user = ?1 ORDER BY us.createdAt DESC")
    List<UserSubscription> findByUserOrderByCreatedAtDesc(User user);
}
