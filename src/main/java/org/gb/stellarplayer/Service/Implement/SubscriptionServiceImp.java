package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Model.Enum.DateType;
import org.gb.stellarplayer.Repository.SubscriptionRepository;
import org.gb.stellarplayer.Request.SubscriptionRequest;
import org.gb.stellarplayer.Service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImp implements SubscriptionService {
    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Override
    public List<Subscription> getSubscription() {
        return subscriptionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Subscription::getPrice))
                .collect(Collectors.toList());
    }

    @Override
    public Subscription addSubscription(SubscriptionRequest sr) {
        DateType dateType = DateType.valueOf(sr.getDateType().toUpperCase());
        Subscription subscription = new Subscription().builder()
                .name(sr.getName())
                .price(sr.getPrice())
                .offer(sr.getOffer())
                .description(sr.getDescription())
                .features(sr.getFeatures())
                .dateType(dateType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription updateSubscription(SubscriptionRequest sr, int id) {
        if (!subscriptionRepository.findById(id).isPresent()) {
            throw new BadRequestException("Subscription not found");
        }
        Subscription subscription = subscriptionRepository.findById(id).get();
        DateType dateType = DateType.valueOf(sr.getDateType().toUpperCase());
        subscription.setName(sr.getName());
        subscription.setPrice(sr.getPrice());
        subscription.setOffer(sr.getOffer());
        subscription.setDescription(sr.getDescription());
        subscription.setFeatures(sr.getFeatures());
        subscription.setDateType(dateType);
        subscription.setUpdatedAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription deleteSubscription(int id) {
        Subscription subscription = subscriptionRepository.findById(id).get();
        subscriptionRepository.delete(subscription);
        return subscription;
    }
}