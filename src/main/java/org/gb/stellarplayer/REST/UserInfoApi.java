package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserSubscription;
import org.gb.stellarplayer.Response.SubscriptionStatusResponse;
import org.gb.stellarplayer.Response.UserSubscriptionDTO;
import org.gb.stellarplayer.Response.UserDTO;

import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
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
    @Autowired
    UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public UserDTO getUserInfo(@PathVariable int id, @RequestHeader("Authorization") String token) {
        validateUserAccess(token, id);
        User user = userService.getUserById(id);
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(role -> UserDTO.RoleDTO.builder()
                                .name(role.getName().name())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            User savedUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create user: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateRequest user, 
                                          @RequestHeader("Authorization") String token,
                                          @PathVariable int id) {
        validateUserAccess(token, id);
        userService.updateUser(user, id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/{id}/update/avatar")
    public ResponseEntity<?> updateUserAvatar(@RequestBody UserUpdateRequest user, 
                                            @RequestHeader("Authorization") String token,
                                            @PathVariable int id) {
        validateUserAccess(token, id);
        userService.updateUserAvatar(user, id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/{id}/update/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody UserUpdatePasswordRequest user, 
                                              @RequestHeader("Authorization") String token, 
                                              @PathVariable int id) {
        validateUserAccess(token, id);
        if (user.oldPassword == null || user.newPassword == null) {
            return new ResponseEntity<>("Old password and new password must not be null", HttpStatus.BAD_REQUEST);
        }
        userService.updateUserPassword(user, id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, 
                                      @RequestBody User user,
                                      @RequestHeader("Authorization") String token) {
        validateUserAccess(token, id);
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id, 
                                      @RequestHeader("Authorization") String token) {
        validateUserAccess(token, id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete user: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/subscription")
    public ResponseEntity<SubscriptionStatusResponse> getUserSubscription(@PathVariable int id, 
                                                                        @RequestHeader("Authorization") String token) {
        validateUserAccess(token, id);
        UserSubscription subscription = userSubscriptionService.getUserSubscriptionByUserId(id);

        if (subscription == null) {
            return new ResponseEntity<>(SubscriptionStatusResponse.noSubscription(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(SubscriptionStatusResponse.activeSubscription(subscription), HttpStatus.OK);
        }
    }

    private void validateAdminToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                if (!hasAdminRole(user)) {
                    throw new BadRequestException("Access denied. Admin privileges required");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    private void validateUserAccess(String token, int userId) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                if (!hasAdminRole(user) && user.getId() != userId) {
                    throw new BadRequestException("Access denied. You can only access your own profile");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
    }

    
}
