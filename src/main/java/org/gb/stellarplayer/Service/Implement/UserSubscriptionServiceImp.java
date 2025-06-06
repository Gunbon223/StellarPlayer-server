package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserSubscription;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.SubscriptionRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Repository.UserSubscriptionRepository;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.gb.stellarplayer.Ultils.DateTypeToNum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserSubscriptionServiceImp implements UserSubscriptionService {
    @Autowired
    UserSubscriptionRepository userSubscriptionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;






    @Override
    @Transactional
    public UserSubscription getUserSubscriptionByUserId(int user_id) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new BadRequestException("User not found"));

        // Get active subscriptions ordered by end date (most recent first)
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository.findActiveSubscriptionsByUserOrderByEndDateDesc(user);

        // Check if any subscriptions have expired
        LocalDateTime now = LocalDateTime.now();
        boolean hasExpired = false;

        for (UserSubscription subscription : activeSubscriptions) {
            if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(now)) {
                // Update the subscription to inactive
                subscription.setActive(false);
                subscription.setUpdatedAt(now);
                userSubscriptionRepository.save(subscription);
                hasExpired = true;
            }
        }

        // If any subscriptions have expired, create a free plan subscription
        if (hasExpired) {
        }

        // Get the updated list of active subscriptions
        activeSubscriptions = userSubscriptionRepository.findActiveSubscriptionsByUserOrderByEndDateDesc(user);

        // Return the first one (most recent) or null if none exists
        if (!activeSubscriptions.isEmpty()) {
            return activeSubscriptions.get(0);
        } else {
            // Return null to indicate no active subscription
            return null;
        }
    }



    @Override
    @Transactional
    public UserSubscription addUserSubscription(int user_id, int subscription_id) {
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Subscription subscription = subscriptionRepository.findById(subscription_id)
                .orElseThrow(() -> new BadRequestException("Subscription not found"));

        // Deactivate any existing active subscriptions for this user
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository.findByUserAndIsActiveTrue(user);
        LocalDateTime now = LocalDateTime.now();

        for (UserSubscription existingSubscription : activeSubscriptions) {
            existingSubscription.setActive(false);
            existingSubscription.setUpdatedAt(now);
            userSubscriptionRepository.save(existingSubscription);
        }

        // Calculate subscription duration based on date type
        int daySub = DateTypeToNum.convert(subscription.getDateType());

        // Create and save the new subscription
        UserSubscription userSubscription = new UserSubscription().builder()
                .user(user)
                .subscription(subscription)
                .createdAt(now)
                .updatedAt(now)
                .isActive(true)
                .startDate(now)
                .endDate(now.plusDays(daySub))
                .build();

        return userSubscriptionRepository.save(userSubscription);
    }


    @Override
    public UserSubscription updateUserSubscription(String username) {
        return null;
    }

    @Override
    public UserSubscription deleteUserSubscription(String username) {
        return null;
    }

}
