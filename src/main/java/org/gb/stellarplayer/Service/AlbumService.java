package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Album;

import java.util.List;

public interface AlbumService {
    List<Album> getAlbums();
    Album getAlbumById(int id);
    Album addAlbum(Album album);
    Album updateAlbum(Album album);
    Album deleteAlbum(int id);

}
