package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Service.UserService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserInfoApi {
    @Autowired
    UserService userService;
    @Autowired
    JwtUtil jwtUtil;
    @GetMapping("/{id}")
    public User getUserInfo(@PathVariable int id, @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7); // remove "Bearer " from the token
        if (!jwtUtil.validateJwtToken(jwt)) {
            throw new BadRequestException("Session expired! Please login again!");
        }
        return userService.getUserById(id);
    }

}
