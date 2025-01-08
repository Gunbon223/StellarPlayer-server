package org.gb.stellarplayer.Service.Implement;

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
    public UserSubscription getUserSubscriptionByUserId(int user_id) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new BadRequestException("User not found"));
        return userSubscriptionRepository.findByUser(user).orElseThrow(() -> new BadRequestException("User Subscription not found!User dont have subscription"));
    }

    @Override
    public List<UserSubscription> getAllUserSubscription() {
        return userSubscriptionRepository.findAll();
    }

    @Override
    public UserSubscription addUserSubscription(int user_id, int subscription_id) {
        if (userRepository.findById(user_id).isPresent() && subscriptionRepository.findById(subscription_id).isPresent()) {
            int daySub = DateTypeToNum.convert(subscriptionRepository.findById(subscription_id).get().getDateType());
            UserSubscription userSubscription = new UserSubscription().builder()
                    .user(userRepository.findById(user_id).get())
                    .subscription(subscriptionRepository.findById(subscription_id).get())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isActive(true)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(daySub))
                    .build();
            return userSubscriptionRepository.save(userSubscription);
        } else {
            throw new BadRequestException("User or Subscription not found");
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
}
