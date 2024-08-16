package org.gb.stellarplayer.Entites.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String name;
    private String email;
    private String password;
    private String avatar;
    private LocalDate dob;
    private String createdAt;
    private String updatedAt;   
}
