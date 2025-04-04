package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.User;
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

    @GetMapping("/{id}/subscription")
    public ResponseEntity<?> getUserSubscription(@PathVariable int id, @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        return new ResponseEntity<>(userSubscriptionService.getUserSubscriptionByUserId(id), HttpStatus.OK);
    }

}
