package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserSubscription;

import java.util.List;
import java.util.Map;

public interface UserSubscriptionService {
    /**
     * Get all subscriptions for a user
     * @param user_id The user ID
     * @return List of user subscriptions
     */

    /**
     * Get all subscriptions
     * @return List of all user subscriptions
     */

    /**
     * Add a subscription for a user
     * @param user_id The user ID
     * @param subscription_id The subscription ID
     * @return The created user subscription
     */
    UserSubscription addUserSubscription(int user_id, int subscription_id);

    /**
     * Update a user subscription
     * @param username The username
     * @return The updated user subscription
     */
    UserSubscription updateUserSubscription(String username);

    /**
     * Delete a user subscription
     * @param username The username
     * @return The deleted user subscription
     */
    UserSubscription deleteUserSubscription(String username);

    /**
     * For backward compatibility - returns the most recent active subscription
     * @param user_id The user ID
     * @return The most recent active subscription, or a default free plan if none exists
     */
    UserSubscription getUserSubscriptionByUserId(int user_id);

    /**
     * Get the total count of users with active subscriptions
     * @return The total count
     */
    long getTotalActiveSubscriptionsCount();

    /**
     * Get new subscription counts by period
     * @param period The period (month, quarter, year)
     * @return Map of period to count
     */
    Map<String, Long> getNewSubscriptionsCountByPeriod(String period);
}
