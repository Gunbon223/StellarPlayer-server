package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private List<TrackSearchDTO> tracks;
    private List<AlbumSearchDTO> albums;
    private List<GenreSearchDTO> genres;
    private List<PlaylistSearchDTO> playlists;
    private List<ArtistSearchDTO> artists;
}
