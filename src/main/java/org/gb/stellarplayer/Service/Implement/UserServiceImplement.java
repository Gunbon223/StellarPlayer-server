package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Exception.ResourceNotFoundException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.UserUpdatePasswordRequest;
import org.gb.stellarplayer.Request.UserUpdateRequest;
import org.gb.stellarplayer.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImplement implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public User updateUser(UserUpdateRequest userUpdateRequest,int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmail(userUpdateRequest.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User updateUserAvatar(UserUpdateRequest userUpdateRequest, int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAvatar(userUpdateRequest.getAvatar());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User updateUserPassword(UserUpdatePasswordRequest userUpdateRequest, int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(userUpdateRequest.oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(userUpdateRequest.newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User getSubscribedUserById(int id) {
//        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        if (user.getSubscription() == null) {
//            throw new BadRequestException("User not subscribed");
//        }
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findByName(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(null);
        return user;
    }

    @Override
    public User getUserById(int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(null);
        return user;
    }
}
