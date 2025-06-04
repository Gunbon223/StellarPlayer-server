package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ArtistService {
    List<Artist> getArtists();
    Artist getArtistById(int id);
    Artist addArtist(Artist artist);
    Artist updateArtist(Artist artist);
    void deleteArtist(int id);
    Page<Track> getTracksByArtistId(int artistId, Pageable pageable);
    Page<Album> getAlbumsByArtistId(int artistId, Pageable pageable);
} 