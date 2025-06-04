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
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        Subscription newSubscription = subscriptionRepository.findById(subscription_id)
                .orElseThrow(() -> new BadRequestException("Subscription not found"));

        LocalDateTime now = LocalDateTime.now();
        int daysToAdd = DateTypeToNum.convert(newSubscription.getDateType());

        // Check if user has an active subscription
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository.findByUserAndIsActiveTrue(user);

        if (!activeSubscriptions.isEmpty()) {
            // User has an active subscription
            UserSubscription existingSubscription = activeSubscriptions.get(0);
            Subscription currentSubscription = existingSubscription.getSubscription();
            
            // Compare subscription durations to find which has longer duration
            int currentSubscriptionDays = DateTypeToNum.convert(currentSubscription.getDateType());
            
            // Determine which subscription to use based on duration
            Subscription subscriptionToUse;
            if (currentSubscriptionDays >= daysToAdd) {
                // Keep the current subscription as it has longer or equal duration
                subscriptionToUse = currentSubscription;
            } else {
                // Use the new subscription as it has longer duration
                subscriptionToUse = newSubscription;
            }
            
            // Always extend the subscription duration by adding days to current end date
            LocalDateTime newEndDate = existingSubscription.getEndDate().plusDays(daysToAdd);
            
            // Update the existing subscription
            existingSubscription.setEndDate(newEndDate);
            existingSubscription.setUpdatedAt(now);
            existingSubscription.setSubscription(subscriptionToUse);
            
            return userSubscriptionRepository.save(existingSubscription);
        } else {
            // No active subscription, create a new one
            UserSubscription userSubscription = new UserSubscription().builder()
                    .user(user)
                    .subscription(newSubscription)
                    .createdAt(now)
                    .updatedAt(now)
                    .isActive(true)
                    .startDate(now)
                    .endDate(now.plusDays(daysToAdd))
                    .build();

            return userSubscriptionRepository.save(userSubscription);
        }
    }

    @Override
    public UserSubscription updateUserSubscription(String username) {
        return null;
    }

    @Override
    public UserSubscription deleteUserSubscription(String username) {
        return null;
    }

    @Override
    public long getTotalActiveSubscriptionsCount() {
        return userSubscriptionRepository.findByIsActiveTrue().size();
    }

    @Override
    public Map<String, Long> getNewSubscriptionsCountByPeriod(String period) {
        List<UserSubscription> allSubscriptions = userSubscriptionRepository.findAll();
        Map<String, Long> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "month":
                // Get current month data
                YearMonth currentMonth = YearMonth.now();
                LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
                
                long currentMonthSubscriptions = allSubscriptions.stream()
                        .filter(subscription -> subscription.getCreatedAt() != null && 
                                subscription.getCreatedAt().isAfter(startOfMonth) && 
                                subscription.getCreatedAt().isBefore(now))
                        .count();
                result.put("current_month", currentMonthSubscriptions);
                break;
                
            case "quarter":
                // Current quarter (3 months)
                LocalDateTime threeMonthsAgo = now.minusMonths(3);
                
                long quarterlySubscriptions = allSubscriptions.stream()
                        .filter(subscription -> subscription.getCreatedAt() != null && 
                                subscription.getCreatedAt().isAfter(threeMonthsAgo) && 
                                subscription.getCreatedAt().isBefore(now))
                        .count();
                result.put("last_3_months", quarterlySubscriptions);
                
                // Monthly breakdown for the quarter
                for (int i = 0; i < 3; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    long monthlySubscriptions = allSubscriptions.stream()
                            .filter(subscription -> subscription.getCreatedAt() != null && 
                                    subscription.getCreatedAt().isAfter(startOfPastMonth) && 
                                    subscription.getCreatedAt().isBefore(endOfPastMonth.plusDays(1)))
                            .count();
                    result.put(month.toString(), monthlySubscriptions);
                }
                break;
                
            case "year":
                // Current year
                LocalDateTime oneYearAgo = now.minusYears(1);
                
                long yearlySubscriptions = allSubscriptions.stream()
                        .filter(subscription -> subscription.getCreatedAt() != null && 
                                subscription.getCreatedAt().isAfter(oneYearAgo) && 
                                subscription.getCreatedAt().isBefore(now))
                        .count();
                result.put("last_12_months", yearlySubscriptions);
                
                // Monthly breakdown for the year
                for (int i = 0; i < 12; i++) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    LocalDateTime startOfPastMonth = month.atDay(1).atStartOfDay();
                    LocalDateTime endOfPastMonth = month.atEndOfMonth().atTime(23, 59, 59);
                    
                    long monthlySubscriptions = allSubscriptions.stream()
                            .filter(subscription -> subscription.getCreatedAt() != null && 
                                    subscription.getCreatedAt().isAfter(startOfPastMonth) && 
                                    subscription.getCreatedAt().isBefore(endOfPastMonth.plusDays(1)))
                            .count();
                    result.put(month.toString(), monthlySubscriptions);
                }
                break;
                
            default:
                throw new BadRequestException("Invalid period. Use 'month', 'quarter', or 'year'");
        }
        
        return result;
    }
}
