package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Exception.ResourceNotFoundException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.UserUpdateRequest;
import org.gb.stellarplayer.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImplement implements UserService {
    @Autowired
    UserRepository userRepository;

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
