package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Request.SubscriptionRequest;

import java.util.List;

public interface SubscriptionService {
    List<Subscription> getSubscription();
    Subscription addSubscription(SubscriptionRequest sr);
    Subscription updateSubscription(SubscriptionRequest sr,int id);
    Subscription deleteSubscription(int id);

}
