package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Album;

import java.time.LocalDate;
import java.util.List;

public interface AlbumService {
    List<Album> getAlbums();
    Album getAlbumById(int id);
    Album addAlbum(Album album);
    Album updateAlbum(Album album);
    Album deleteAlbum(int id);
    List<Album> getAllAlbums();
    Album createAlbum(Album album);
    List<Album> getAlbumsByArtistId(int artistId);
    List<Album> getAlbumsByReleaseDateRange(LocalDate startDate, LocalDate endDate);
    void deleteAlbumWithCascade(int albumId);
}
