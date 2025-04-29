package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserSubscription;
import org.gb.stellarplayer.Response.SubscriptionStatusResponse;
import org.gb.stellarplayer.Response.UserSubscriptionDTO;

import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserSubscriptionRepository;
import org.gb.stellarplayer.Request.UserUpdatePasswordRequest;
import org.gb.stellarplayer.Request.UserUpdateRequest;
import org.gb.stellarplayer.Service.UserService;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserInfoApi {
    @Autowired
    UserService userService;
    @Autowired
    UserSubscriptionService userSubscriptionService;
    @Autowired
    JwtUtil jwtUtil;
    @GetMapping("/{id}")
    public User getUserInfo(@PathVariable int id, @RequestHeader("Authorization") String token) {
        log.info(token);
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        return userService.getUserById(id);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateRequest user, @RequestHeader("Authorization") String token,@PathVariable int id) {
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        userService.updateUser(user,id);
        return new ResponseEntity<>(user, HttpStatus.OK); //tra ve 200

    }

    @PostMapping("/{id}/update/avatar")
    public ResponseEntity<?> updateUserAvatar(@RequestBody UserUpdateRequest user, @RequestHeader("Authorization") String token,@PathVariable int id) {
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        userService.updateUserAvatar(user,id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/{id}/update/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody UserUpdatePasswordRequest user, @RequestHeader("Authorization") String token, @PathVariable int id) {
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        if (user.oldPassword == null || user.newPassword == null) {
            return new ResponseEntity<>("Old password and new password must not be null", HttpStatus.BAD_REQUEST);
        }
        userService.updateUserPassword(user,id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    /**
     * Get the active subscription for a user
     * This endpoint returns the most recent active subscription or a message if none exists
     */
    @GetMapping("/{id}/subscription")
    public ResponseEntity<SubscriptionStatusResponse> getUserSubscription(@PathVariable int id, @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);

        UserSubscription subscription = userSubscriptionService.getUserSubscriptionByUserId(id);

        if (subscription == null) {
            // User has no active subscription
            return new ResponseEntity<>(SubscriptionStatusResponse.noSubscription(), HttpStatus.OK);
        } else {
            // User has an active subscription
            return new ResponseEntity<>(SubscriptionStatusResponse.activeSubscription(subscription), HttpStatus.OK);
        }
    }

    /**
     * Get all subscriptions for a user
     * This endpoint returns all subscriptions for a user, including inactive ones
     */
    @GetMapping("/{id}/subscriptions")
    public ResponseEntity<?> getUserSubscriptions(@PathVariable int id, @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);

        // Get the user's subscription (users can only have one subscription at a time)
        UserSubscription subscription = userSubscriptionService.getUserSubscriptionByUserId(id);

        if (subscription == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "You don't have any subscription. Please purchase a subscription to access premium features.");
            response.put("subscription", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            // Convert to DTO and return as a single object, not a list
            UserSubscriptionDTO dto = UserSubscriptionDTO.fromEntity(subscription);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
    }

    /**
     * Get the active subscription for a user
     * This endpoint returns only the most recent active subscription, or a message if none exists
     */

}
