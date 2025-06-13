package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Model.Enum.PlaylistType;
import org.gb.stellarplayer.Response.AlbumSearchDTO;
import org.gb.stellarplayer.Response.PlaylistSearchDTO;
import org.gb.stellarplayer.DTO.ArtistRadioPlaylistDTO;

import java.util.List;

public interface RecommendationService {
    
    // System-wide recommendation playlists
    Playlist generateTrendingWeekly();
    Playlist generateNewMusicDaily();
    Playlist generateNewReleases();
    Playlist generateViralHits();
    
    // User-specific recommendation playlists
    Playlist generateUserDiscoveryWeekly(Integer userId);
    Playlist generateUserArtistMix(Integer userId);
    Playlist generateUserGenreMix(Integer userId);
    
    // New history and favorite-based recommendations
    List<AlbumSearchDTO> getRecommendedAlbumsBasedOnHistory(Integer userId, int limit);
    List<ArtistRadioPlaylistDTO> getArtistRadioPlaylists(Integer userId, int limit);
    Playlist generateArtistRadio(Integer userId, Integer artistId);
    void refreshArtistRadioPlaylists(Integer userId);
    
    // Utility methods
    List<Track> getTrendingTracks(int limit);
    List<Track> getNewTracks(int days, int limit);
    List<Track> getViralTracks(int limit);
    List<Track> getRecommendedTracksForUser(Integer userId, int limit);
    
    // Batch operations
    void updateSystemPlaylists();
    void updateUserPlaylists(Integer userId);
    void updateAllUserArtistRadios();
    
    // Get recommendation playlists
    List<Playlist> getRecommendationPlaylists();
    List<Playlist> getUserRecommendationPlaylists(Integer userId);
} 