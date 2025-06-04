package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.UserFavouritePlaylist;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouritePlaylistDTO {
    private Integer id;
    private String name;
    private String cover;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime favouriteAddedAt;

    public static FavouritePlaylistDTO fromEntity(UserFavouritePlaylist userFavouritePlaylist) {
        return FavouritePlaylistDTO.builder()
                .id(userFavouritePlaylist.getPlaylist().getId())
                .name(userFavouritePlaylist.getPlaylist().getName())
                .cover(userFavouritePlaylist.getPlaylist().getCover())
                .status(userFavouritePlaylist.getPlaylist().isStatus())
                .createdAt(userFavouritePlaylist.getPlaylist().getCreatedAt())
                .updatedAt(userFavouritePlaylist.getPlaylist().getUpdatedAt())
                .favouriteAddedAt(userFavouritePlaylist.getCreatedAt())
                .build();
    }
} 