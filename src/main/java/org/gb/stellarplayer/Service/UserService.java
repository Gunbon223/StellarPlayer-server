package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Request.UserUpdatePasswordRequest;
import org.gb.stellarplayer.Request.UserUpdateRequest;

public interface UserService {
    User getUserByUsername(String username);
    User getUserById(int id);
    User updateUser(UserUpdateRequest userUpdateRequest, int id);
    User updateUserAvatar(UserUpdateRequest userUpdateRequest, int id);
    User updateUserPassword(UserUpdatePasswordRequest userUpdateRequest, int id);
    User getSubscribedUserById(int id);
}
