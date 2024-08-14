package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Request.UserUpdateRequest;
import org.gb.stellarplayer.Service.UserService;
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
    JwtUtil jwtUtil;
    @GetMapping("/{id}")
    public User getUserInfo(@PathVariable int id, @RequestHeader("Authorization") String token) {
        log.info(token);
        String jwt = token.substring(7);
        if (!jwtUtil.validateJwtToken(jwt)) {
            throw new BadRequestException("Session expired! Please login again!");
        }
        return userService.getUserById(id);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateRequest user, @RequestHeader("Authorization") String token,@PathVariable int id) {
        String jwt = token.substring(7);
        if (!jwtUtil.validateJwtToken(jwt)) {
            return new ResponseEntity<>("Session expired! Please login again!", HttpStatus.BAD_REQUEST);
        }
        userService.updateUser(user,id);
        return new ResponseEntity<>(user, HttpStatus.OK); //tra ve 200

    }

}
