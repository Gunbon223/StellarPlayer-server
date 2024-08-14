package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Request.UserUpdateRequest;

public interface UserService {
    User getUserByUsername(String username);
    User getUserById(int id);
    User updateUser(UserUpdateRequest userUpdateRequest, int id);
}
