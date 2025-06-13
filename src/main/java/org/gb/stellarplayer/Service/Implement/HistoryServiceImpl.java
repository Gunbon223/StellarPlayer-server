package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.Repository.*;
import org.gb.stellarplayer.Service.HistoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoryServiceImpl.class);
    
    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final ArtistRepository artistRepository;
    
    private static final int RECENT_DAYS = 3;
    private static final int RECENT_ITEMS_LIMIT = 5;
    
    @Override
    @Transactional
    public void recordTrackPlay(Integer userId, Integer trackId) {
        User user = userRepository.findById(userId).orElse(null);
        Track track = trackRepository.findById(trackId).orElse(null);
        
        if (user != null && track != null) {
            logger.info("Recording track play for user {} and track {}", userId, trackId);
            
            // Check if a history record already exists for this user-track combination
            List<History> existingHistories = historyRepository.findByUserIdAndTrackId(userId, trackId);
            
            logger.info("Found {} existing histories for user {} and track {}", existingHistories.size(), userId, trackId);
            
            History history;
            if (!existingHistories.isEmpty()) {
                // Update existing record
                logger.info("Updating existing history record with ID {}", existingHistories.get(0).getId());
                history = existingHistories.get(0); // Get the first one
                history.setUpdatedAt(LocalDateTime.now());
                history.setPlayedAt(LocalDateTime.now()); // Update play time
            } else {
                // Create new record
                logger.info("Creating new history record for user {} and track {}", userId, trackId);
                history = History.builder()
                    .user(user)
                    .track(track)
                    .album(track.getAlbum()) // Also record the album if available
                    .historyType(History.HistoryType.TRACK_PLAY)
                    .playedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            }
            
            try {
                historyRepository.save(history);
                logger.info("Successfully saved history record with ID {}", history.getId());
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    logger.warn("Duplicate entry detected for user {} and track {}, ignoring...", userId, trackId);
                    // Find the existing record and update it
                    List<History> duplicateHistories = historyRepository.findByUserIdAndTrackId(userId, trackId);
                    if (!duplicateHistories.isEmpty()) {
                        History existing = duplicateHistories.get(0);
                        existing.setPlayedAt(LocalDateTime.now());
                        existing.setUpdatedAt(LocalDateTime.now());
                        historyRepository.save(existing);
                        logger.info("Updated existing history record with ID {}", existing.getId());
                    }
                } else {
                    logger.error("Error saving history record: {}", e.getMessage());
                    throw e;
                }
            }
        }
    }
    
    @Override
    public void recordAlbumPlay(Integer userId, Integer albumId) {
        User user = userRepository.findById(userId).orElse(null);
        Album album = albumRepository.findById(albumId).orElse(null);
        
        if (user != null && album != null) {
            // Check if a history record already exists for this user-album combination
            List<History> existingHistories = historyRepository.findByUserIdAndAlbumId(userId, albumId);
            
            History history;
            if (!existingHistories.isEmpty()) {
                // Update existing record
                history = existingHistories.get(0);
                history.setUpdatedAt(LocalDateTime.now());
                history.setPlayedAt(LocalDateTime.now());
            } else {
                // Create new record
                history = History.builder()
                    .user(user)
                    .album(album)
                    .historyType(History.HistoryType.ALBUM_PLAY)
                    .playedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            }
            
            historyRepository.save(history);
        }
    }
    
    @Override
    public void recordPlaylistPlay(Integer userId, Integer playlistId) {
        User user = userRepository.findById(userId).orElse(null);
        Playlist playlist = playlistRepository.findById(playlistId).orElse(null);
        
        if (user != null && playlist != null) {
            // Check if a history record already exists for this user-playlist combination
            List<History> existingHistories = historyRepository.findByUserIdAndPlaylistId(userId, playlistId);
            
            History history;
            if (!existingHistories.isEmpty()) {
                // Update existing record
                history = existingHistories.get(0);
                history.setUpdatedAt(LocalDateTime.now());
                history.setPlayedAt(LocalDateTime.now());
            } else {
                // Create new record
                history = History.builder()
                    .user(user)
                    .playlist(playlist)
                    .historyType(History.HistoryType.PLAYLIST_PLAY)
                    .playedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            }
            
            historyRepository.save(history);
        }
    }
    
    @Override
    public void recordArtistPlay(Integer userId, Integer artistId) {
        User user = userRepository.findById(userId).orElse(null);
        Artist artist = artistRepository.findById(artistId).orElse(null);
        
        if (user != null && artist != null) {
            // Check if a history record already exists for this user-artist combination
            List<History> existingHistories = historyRepository.findByUserIdAndArtistId(userId, artistId);
            
            History history;
            if (!existingHistories.isEmpty()) {
                // Update existing record
                history = existingHistories.get(0);
                history.setUpdatedAt(LocalDateTime.now());
                history.setPlayedAt(LocalDateTime.now());
            } else {
                // Create new record
                history = History.builder()
                    .user(user)
                    .artist(artist)
                    .historyType(History.HistoryType.ARTIST_PLAY)
                    .playedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            }
            
            historyRepository.save(history);
        }
    }
    
    @Override
    public List<Track> getRecentlyPlayedTracks(Integer userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(RECENT_DAYS);
        Pageable pageable = PageRequest.of(0, RECENT_ITEMS_LIMIT);
        return historyRepository.findRecentlyPlayedTracks(userId, threeDaysAgo, pageable);
    }
    
    @Override
    public Map<String, Object> getRecentlyPlayedItems(Integer userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(RECENT_DAYS);
        Pageable pageable = PageRequest.of(0, RECENT_ITEMS_LIMIT);
        
        List<Album> recentAlbums = historyRepository.findRecentlyPlayedAlbums(userId, threeDaysAgo, pageable);
        List<Playlist> recentPlaylists = historyRepository.findRecentlyPlayedPlaylists(userId, threeDaysAgo, pageable);
        List<Artist> recentArtists = historyRepository.findRecentlyPlayedArtists(userId, threeDaysAgo, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("albums", recentAlbums);
        result.put("playlists", recentPlaylists);
        result.put("artists", recentArtists);
        result.put("period", RECENT_DAYS + " days");
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }
    
    @Override
    public List<Album> getRecentlyPlayedAlbums(Integer userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(RECENT_DAYS);
        Pageable pageable = PageRequest.of(0, RECENT_ITEMS_LIMIT);
        return historyRepository.findRecentlyPlayedAlbums(userId, threeDaysAgo, pageable);
    }
    
    @Override
    public List<Playlist> getRecentlyPlayedPlaylists(Integer userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(RECENT_DAYS);
        Pageable pageable = PageRequest.of(0, RECENT_ITEMS_LIMIT);
        return historyRepository.findRecentlyPlayedPlaylists(userId, threeDaysAgo, pageable);
    }
    
    @Override
    public List<Artist> getRecentlyPlayedArtists(Integer userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(RECENT_DAYS);
        Pageable pageable = PageRequest.of(0, RECENT_ITEMS_LIMIT * 2); // Get more to combine and deduplicate
        
        // Get artists from track plays
        List<Artist> trackArtists = historyRepository.findRecentlyPlayedArtists(userId, threeDaysAgo, pageable);
        
        // Get artists from direct artist plays
        List<Artist> directArtists = historyRepository.findRecentlyPlayedArtistsDirectly(userId, threeDaysAgo, pageable);
        
        // Combine and deduplicate
        List<Artist> combinedArtists = new java.util.ArrayList<>();
        combinedArtists.addAll(directArtists);
        
        for (Artist trackArtist : trackArtists) {
            if (!combinedArtists.stream().anyMatch(a -> a.getId().equals(trackArtist.getId()))) {
                combinedArtists.add(trackArtist);
            }
        }
        
        // Limit to final count
        return combinedArtists.stream().limit(RECENT_ITEMS_LIMIT).collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public List<History> getUserHistory(Integer userId, int page, int size) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(RECENT_DAYS);
        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findUserHistorySince(userId, threeDaysAgo, pageable);
    }
    
    @Override
    @Transactional
    public void cleanupOldHistory(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        long deletedCount = historyRepository.deleteHistoryOlderThan(cutoffDate);
        logger.info("Cleaned up {} old history records older than {} days", deletedCount, daysToKeep);
    }
    
    @Override
    @Transactional
    public void cleanupUserHistory(Integer userId, int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        long deletedCount = historyRepository.deleteUserHistoryOlderThan(userId, cutoffDate);
        logger.info("Cleaned up {} old history records for user {} older than {} days", deletedCount, userId, daysToKeep);
    }
    
    @Override
    @Transactional
    public long deleteHistoryOlderThan(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return historyRepository.deleteHistoryOlderThan(cutoffDate);
    }
    
    @Override
    public long getHistoryCount() {
        return historyRepository.countAllHistory();
    }
    
    @Override
    public Map<String, Object> getHistoryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalRecords = historyRepository.countAllHistory();
        long recentRecords = historyRepository.countRecentHistory(LocalDateTime.now().minusDays(7));
        long oldRecords = historyRepository.countHistoryOlderThan(LocalDateTime.now().minusDays(30));
        
        // Get oldest and newest records
        List<History> oldest = historyRepository.findOldestHistory(PageRequest.of(0, 1));
        List<History> newest = historyRepository.findNewestHistory(PageRequest.of(0, 1));
        
        stats.put("total_records", totalRecords);
        stats.put("recent_records_7_days", recentRecords);
        stats.put("old_records_30_plus_days", oldRecords);
        stats.put("oldest_record_date", oldest.isEmpty() ? null : oldest.get(0).getPlayedAt());
        stats.put("newest_record_date", newest.isEmpty() ? null : newest.get(0).getPlayedAt());
        stats.put("database_size_recommendation", totalRecords > 100000 ? "CLEANUP_RECOMMENDED" : "OK");
        
        return stats;
    }
    
    @Override
    @Transactional
    public void optimizeHistoryStorage() {
        // Delete records older than 90 days by default
        final int DEFAULT_RETENTION_DAYS = 90;
        
        // Get statistics before cleanup
        long totalBefore = getHistoryCount();
        long oldRecords = historyRepository.countHistoryOlderThan(LocalDateTime.now().minusDays(DEFAULT_RETENTION_DAYS));
        
        if (oldRecords > 0) {
            // Perform cleanup
            long deletedCount = deleteHistoryOlderThan(DEFAULT_RETENTION_DAYS);
            long totalAfter = getHistoryCount();
            
            logger.info("History optimization completed:");
            logger.info("- Records before cleanup: {}", totalBefore);
            logger.info("- Records deleted: {}", deletedCount);
            logger.info("- Records after cleanup: {}", totalAfter);
            logger.info("- Space saved: {}%", (deletedCount * 100.0) / totalBefore);
        } else {
            logger.info("No old records found. No cleanup needed.");
        }
    }
} 