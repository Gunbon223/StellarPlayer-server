package org.gb.stellarplayer.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserSubscription;
import org.gb.stellarplayer.Repository.SubscriptionRepository;
import org.gb.stellarplayer.Repository.UserSubscriptionRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Configuration class for scheduling subscription-related tasks
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Log4j2
public class SubscriptionSchedulingConfig {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Scheduled task to update expired subscriptions
     * Runs every hour to check for expired subscriptions and set them as inactive
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void updateExpiredSubscriptions() {
        log.info("Running scheduled task to update expired subscriptions at {}", LocalDateTime.now());

        try {
            // Get all active subscriptions
            List<UserSubscription> activeSubscriptions = userSubscriptionRepository.findByIsActiveTrue();
            int updatedCount = 0;

            LocalDateTime now = LocalDateTime.now();

            for (UserSubscription subscription : activeSubscriptions) {
                // Check if the subscription has an end date and if it has expired
                if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(now)) {
                    User user = subscription.getUser();

                    try {
                        // Process each subscription in its own transaction
                        processExpiredSubscription(subscription, user);
                        updatedCount++;
                    } catch (Exception e) {
                        log.error("Error processing expired subscription for User ID {}: {}",
                                user.getId(), e.getMessage(), e);
                        // Continue with the next subscription
                    }
                }
            }

            log.info("Successfully updated {} expired subscriptions", updatedCount);
        } catch (Exception e) {
            log.error("Error updating expired subscriptions: {}", e.getMessage(), e);
        }
    }

    /**
     * Process an expired subscription - deactivate it and create a free plan
     * Each subscription is processed in its own transaction
     *
     * @param subscription The expired subscription
     * @param user The user
     */
    @Transactional
    public void processExpiredSubscription(UserSubscription subscription, User user) {
        LocalDateTime now = LocalDateTime.now();

        // Update the subscription to inactive
        subscription.setActive(false);
        subscription.setUpdatedAt(now);
        userSubscriptionRepository.save(subscription);

        log.info("Expired subscription deactivated: User ID {}, Subscription ID {}, End Date {}",
                user.getId(),
                subscription.getSubscription().getId(),
                subscription.getEndDate());

        // No longer create a free plan subscription when a subscription expires
    }


}
