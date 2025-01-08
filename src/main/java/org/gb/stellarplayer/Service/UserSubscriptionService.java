package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.UserSubscription;

import java.util.List;

public interface UserSubscriptionService {
    UserSubscription getUserSubscriptionByUserId(int user_id);
   List<UserSubscription> getAllUserSubscription();
    UserSubscription addUserSubscription(int user_id, int subscription_id);
    UserSubscription updateUserSubscription(String username);
    UserSubscription deleteUserSubscription(String username);

}
