package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Exception.ResourceNotFoundException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.UserUpdateRequest;
import org.gb.stellarplayer.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplement implements UserService {
    @Override
    public User updateUser(UserUpdateRequest userUpdateRequest) {
        return null;
    }

    @Autowired
    UserRepository userRepository;
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
