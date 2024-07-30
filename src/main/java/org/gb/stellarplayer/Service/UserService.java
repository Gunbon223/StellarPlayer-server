package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.User;

public interface UserService {
    User getUserByUsername(String username);
    User getUserById(int id);
}
