package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.UserSubscription;

/**
 * Response class for subscription status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusResponse {
    private boolean hasActiveSubscription;
    private String message;
    private UserSubscriptionDTO subscription;
    
    /**
     * Create a response for a user with no subscription
     * 
     * @return SubscriptionStatusResponse with appropriate message
     */
    public static SubscriptionStatusResponse noSubscription() {
        return SubscriptionStatusResponse.builder()
                .hasActiveSubscription(false)
                .message("Free User")
                .subscription(null)
                .build();
    }
    
    /**
     * Create a response for a user with an active subscription
     * 
     * @param subscription The user's subscription
     * @return SubscriptionStatusResponse with subscription details
     */
    public static SubscriptionStatusResponse activeSubscription(UserSubscription subscription) {
        return SubscriptionStatusResponse.builder()
                .hasActiveSubscription(true)
                .message("You have an active subscription.")
                .subscription(UserSubscriptionDTO.fromEntity(subscription))
                .build();
    }
    
    /**
     * Create a response for a user with an expired subscription
     * 
     * @return SubscriptionStatusResponse with appropriate message
     */
    public static SubscriptionStatusResponse expiredSubscription() {
        return SubscriptionStatusResponse.builder()
                .hasActiveSubscription(false)
                .message("Your subscription has expired. Please renew your subscription to continue accessing premium features.")
                .subscription(null)
                .build();
    }
}
