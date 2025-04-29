package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.UserSubscription;

import java.time.LocalDateTime;

/**
 * DTO for simplified UserSubscription response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionDTO {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer subscriptionId;
    private String subscriptionName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;

    /**
     * Convert UserSubscription entity to UserSubscriptionDTO
     */
    public static UserSubscriptionDTO fromEntity(UserSubscription userSubscription) {
        if (userSubscription == null) {
            return null;
        }

        // Check if the subscription has expired but is still marked as active
        boolean isActive = userSubscription.isActive();
        if (isActive && userSubscription.getEndDate() != null && userSubscription.getEndDate().isBefore(LocalDateTime.now())) {
            isActive = false;
        }

        return UserSubscriptionDTO.builder()
                .id(userSubscription.getId())
                .userId(userSubscription.getUser() != null ? userSubscription.getUser().getId() : null)
                .userName(userSubscription.getUser() != null ? userSubscription.getUser().getName() : null)
                .subscriptionId(userSubscription.getSubscription() != null ? userSubscription.getSubscription().getId() : null)
                .subscriptionName(userSubscription.getSubscription() != null ? userSubscription.getSubscription().getName() : "Free Plan")
                .startDate(userSubscription.getStartDate())
                .endDate(userSubscription.getEndDate())
                .active(isActive)
                .build();
    }
}
