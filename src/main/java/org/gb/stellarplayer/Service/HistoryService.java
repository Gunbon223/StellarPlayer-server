package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.*;

import java.util.List;
import java.util.Map;

public interface HistoryService {
    
    // Record play history
    void recordTrackPlay(Integer userId, Integer trackId);
    void recordAlbumPlay(Integer userId, Integer albumId);
    void recordPlaylistPlay(Integer userId, Integer playlistId);
    void recordArtistPlay(Integer userId, Integer artistId);
    
    // Get recently played items (last 3 days)
    List<Track> getRecentlyPlayedTracks(Integer userId);
    Map<String, Object> getRecentlyPlayedItems(Integer userId);
    
    // Get specific recent items
    List<Album> getRecentlyPlayedAlbums(Integer userId);
    List<Playlist> getRecentlyPlayedPlaylists(Integer userId);
    List<Artist> getRecentlyPlayedArtists(Integer userId);
    
    // Get user's full history with pagination
    List<History> getUserHistory(Integer userId, int page, int size);
    
    // Cleanup methods to optimize database storage
    void cleanupOldHistory(int daysToKeep);
    void cleanupUserHistory(Integer userId, int daysToKeep);
    long deleteHistoryOlderThan(int days);
    long getHistoryCount();
    Map<String, Object> getHistoryStatistics();
    void optimizeHistoryStorage();
} 