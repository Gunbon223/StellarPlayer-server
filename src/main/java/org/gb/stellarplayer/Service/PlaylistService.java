package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;

import java.util.List;

public interface PlaylistService {
    List<Playlist> getPlaylists();
    Playlist getPlaylistById(int id);
    Playlist addPlaylist(Playlist playlist);
    Playlist updatePlaylist(Playlist playlist);
    Playlist deletePlaylist(int id);
    List<Playlist> getAllPlaylists();
    
    // Track management methods
    List<Track> getTracksByPlaylistId(int playlistId);
    Playlist addTrackToPlaylist(int playlistId, int trackId);
    Playlist removeTrackFromPlaylist(int playlistId, int trackId);

    /**
     * Delete playlist with cascade handling for related records
     * @param playlistId Playlist ID to delete
     */
    void deletePlaylistWithCascade(int playlistId);
}
