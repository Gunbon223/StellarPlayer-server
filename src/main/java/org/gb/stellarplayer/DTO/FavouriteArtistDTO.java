package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.UserFavouriteArtist;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteArtistDTO {
    private Integer id;
    private String name;
    private String bio;
    private String avatar;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime favouriteAddedAt;

    public static FavouriteArtistDTO fromEntity(UserFavouriteArtist userFavouriteArtist) {
        return FavouriteArtistDTO.builder()
                .id(userFavouriteArtist.getArtist().getId())
                .name(userFavouriteArtist.getArtist().getName())
                .bio(userFavouriteArtist.getArtist().getBio())
                .avatar(userFavouriteArtist.getArtist().getAvatar())
                .status(userFavouriteArtist.getArtist().getActive() != null ? 
                    userFavouriteArtist.getArtist().getActive() : false)
                .createdAt(userFavouriteArtist.getArtist().getCreatedAt())
                .updatedAt(userFavouriteArtist.getArtist().getUpdatedAt())
                .favouriteAddedAt(userFavouriteArtist.getCreatedAt())
                .build();
    }
} 