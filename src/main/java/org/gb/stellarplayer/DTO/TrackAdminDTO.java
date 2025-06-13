package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Genre;
import org.gb.stellarplayer.Entites.Track;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackAdminDTO {
    private Integer id;
    private String title;
    private int duration;
    private boolean status;
    private String path;
    private String cover;
    private String lyrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer releaseYear;
    private List<Artist> artists;
    private Long playCount;
    private List<String> genres;

    public static TrackAdminDTO fromEntity(Track track) {
        return TrackAdminDTO.builder()
                .id(track.getId())
                .title(track.getTitle())
                .duration(track.getDuration())
                .status(track.isStatus())
                .path(track.getPath())
                .cover(track.getCover())
                .lyrics(track.getLyrics())
                .createdAt(track.getCreatedAt())
                .updatedAt(track.getUpdatedAt())
                .releaseYear(track.getReleaseYear())
                .artists(track.getArtists())
                .playCount(track.getPlayCount())
                .genres(track.getGenres().stream().map(Genre::getName).toList())
                .build();
    }
} 