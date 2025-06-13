package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Request.UserUpdatePasswordRequest;
import org.gb.stellarplayer.Request.UserUpdateRequest;

import java.util.List;

public interface UserService {
    User getUserByUsername(String username);
    User getUserById(int id);
    User updateUser(UserUpdateRequest userUpdateRequest, int id);
    User updateUserAvatar(UserUpdateRequest userUpdateRequest, int id);
    User updateUserPassword(UserUpdatePasswordRequest userUpdateRequest, int id);
    User getSubscribedUserById(int id);
    List<User> getAllUsers();
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(int id);
}
