package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.UserArtist;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserArtistDTO {
    private Integer id;
    private UserInfo user;
    private ArtistInfo artist;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Integer id;
        private String name;
        private String email;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArtistInfo {
        private Integer id;
        private String name;
        private String bio;
        private String avatar;
        private Boolean active;
    }

    public static UserArtistDTO fromEntity(UserArtist userArtist) {
        return UserArtistDTO.builder()
                .id(userArtist.getId())
                .user(UserInfo.builder()
                        .id(userArtist.getUser().getId())
                        .name(userArtist.getUser().getName())
                        .email(userArtist.getUser().getEmail())
                        .avatar(userArtist.getUser().getAvatar())
                        .build())
                .artist(ArtistInfo.builder()
                        .id(userArtist.getArtist().getId())
                        .name(userArtist.getArtist().getName())
                        .bio(userArtist.getArtist().getBio())
                        .avatar(userArtist.getArtist().getAvatar())
                        .active(userArtist.getArtist().getActive())
                        .build())
                .createdAt(userArtist.getCreatedAt())
                .updatedAt(userArtist.getUpdatedAt())
                .build();
    }
} 