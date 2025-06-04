package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.UserFavouriteAlbum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteAlbumDTO {
    private Integer id;
    private String title;
    private String cover;
    private LocalDateTime releaseDate;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Artist> artists;
    private LocalDateTime favouriteAddedAt;

    public static FavouriteAlbumDTO fromEntity(UserFavouriteAlbum userFavouriteAlbum) {
        return FavouriteAlbumDTO.builder()
                .id(userFavouriteAlbum.getAlbum().getId())
                .title(userFavouriteAlbum.getAlbum().getTitle())
                .cover(userFavouriteAlbum.getAlbum().getCover())
                .releaseDate(userFavouriteAlbum.getAlbum().getReleaseDate() != null ? 
                    userFavouriteAlbum.getAlbum().getReleaseDate().atStartOfDay() : null)
                .status(userFavouriteAlbum.getAlbum().isStatus())
                .createdAt(userFavouriteAlbum.getAlbum().getCreatedAt())
                .updatedAt(userFavouriteAlbum.getAlbum().getUpdatedAt())
                .artists(userFavouriteAlbum.getAlbum().getArtists())
                .favouriteAddedAt(userFavouriteAlbum.getCreatedAt())
                .build();
    }
} 