package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Request.SubscriptionRequest;
import org.gb.stellarplayer.Service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionApi {
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getSubscription();
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Subscription> addSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {
        Subscription subscription = subscriptionService.addSubscription(subscriptionRequest);
        return new ResponseEntity<>(subscription, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(@RequestBody SubscriptionRequest subscriptionRequest, @PathVariable int id) {
        Subscription subscription = subscriptionService.updateSubscription(subscriptionRequest, id);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }





}
