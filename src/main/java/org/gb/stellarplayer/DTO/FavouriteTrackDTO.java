package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.UserFavouriteTrack;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteTrackDTO {
    private Integer id;
    private String title;
    private int duration;
    private boolean status;
    private String path;
    private String cover;
    private String lyrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Artist> artists;
    private Album album;
    private LocalDateTime favouriteAddedAt;

    public static FavouriteTrackDTO fromEntity(UserFavouriteTrack userFavouriteTrack) {
        return FavouriteTrackDTO.builder()
                .id(userFavouriteTrack.getTrack().getId())
                .title(userFavouriteTrack.getTrack().getTitle())
                .duration(userFavouriteTrack.getTrack().getDuration())
                .status(userFavouriteTrack.getTrack().isStatus())
                .path(userFavouriteTrack.getTrack().getPath())
                .cover(userFavouriteTrack.getTrack().getCover())
                .lyrics(userFavouriteTrack.getTrack().getLyrics())
                .album(userFavouriteTrack.getTrack().getAlbum())
                .createdAt(userFavouriteTrack.getTrack().getCreatedAt())
                .updatedAt(userFavouriteTrack.getTrack().getUpdatedAt())
                .artists(userFavouriteTrack.getTrack().getArtists())
                .favouriteAddedAt(userFavouriteTrack.getCreatedAt())
                .build();
    }
} 