package org.gb.stellarplayer.Request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserUpdateRequest {
    private String email;
    private String password;
    private LocalDateTime dob;
}
