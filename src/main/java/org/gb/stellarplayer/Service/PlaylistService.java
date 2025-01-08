package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Playlist;

import java.util.List;

public interface PlaylistService {
    List<Playlist> getPlaylists();
    Playlist getPlaylistById(int id);
    Playlist addPlaylist(Playlist playlist);
    Playlist updatePlaylist(Playlist playlist);
    Playlist deletePlaylist(int id);
}
