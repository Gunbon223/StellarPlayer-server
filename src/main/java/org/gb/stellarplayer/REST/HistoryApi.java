package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryApi {
    
    private final HistoryService historyService;
    
    /**
     * Get recently played tracks for a user (last 3 days, limit 5)
     * @param userId User ID
     * @return List of recently played tracks
     */
    @GetMapping("/user/{userId}/tracks")
    public ResponseEntity<List<Track>> getRecentlyPlayedTracks(@PathVariable Integer userId) {
        try {
            List<Track> recentTracks = historyService.getRecentlyPlayedTracks(userId);
            return ResponseEntity.ok(recentTracks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get recently played albums, artists, and playlists for a user (last 3 days, limit 5 each)
     * @param userId User ID
     * @return Map containing albums, artists, and playlists
     */
    @GetMapping("/user/{userId}/items")
    public ResponseEntity<Map<String, Object>> getRecentlyPlayedItems(@PathVariable Integer userId) {
        try {
            Map<String, Object> recentItems = historyService.getRecentlyPlayedItems(userId);
            return ResponseEntity.ok(recentItems);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Record a track play for history tracking
     * @param userId User ID
     * @param trackId Track ID
     * @return Success response
     */
    @PostMapping("/user/{userId}/track/{trackId}")
    public ResponseEntity<Map<String, String>> recordTrackPlay(
            @PathVariable Integer userId, 
            @PathVariable Integer trackId) {
        try {
            historyService.recordTrackPlay(userId, trackId);
            return ResponseEntity.ok(Map.of("message", "Track play recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to record track play: " + e.getMessage()));
        }
    }
    
    /**
     * Record an album play for history tracking
     * @param userId User ID
     * @param albumId Album ID
     * @return Success response
     */
    @PostMapping("/user/{userId}/album/{albumId}")
    public ResponseEntity<Map<String, String>> recordAlbumPlay(
            @PathVariable Integer userId, 
            @PathVariable Integer albumId) {
        try {
            historyService.recordAlbumPlay(userId, albumId);
            return ResponseEntity.ok(Map.of("message", "Album play recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to record album play: " + e.getMessage()));
        }
    }
    
    /**
     * Record a playlist play for history tracking
     * @param userId User ID
     * @param playlistId Playlist ID
     * @return Success response
     */
    @PostMapping("/user/{userId}/playlist/{playlistId}")
    public ResponseEntity<Map<String, String>> recordPlaylistPlay(
            @PathVariable Integer userId, 
            @PathVariable Integer playlistId) {
        try {
            historyService.recordPlaylistPlay(userId, playlistId);
            return ResponseEntity.ok(Map.of("message", "Playlist play recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to record playlist play: " + e.getMessage()));
        }
    }
    
    /**
     * Record an artist play for history tracking
     * @param userId User ID
     * @param artistId Artist ID
     * @return Success response
     */
    @PostMapping("/user/{userId}/artist/{artistId}")
    public ResponseEntity<Map<String, String>> recordArtistPlay(
            @PathVariable Integer userId, 
            @PathVariable Integer artistId) {
        try {
            historyService.recordArtistPlay(userId, artistId);
            return ResponseEntity.ok(Map.of("message", "Artist play recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to record artist play: " + e.getMessage()));
        }
    }
    
    // === CLEANUP AND OPTIMIZATION ENDPOINTS === //
    
    /**
     * Get history database statistics
     * @return Database statistics and recommendations
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getHistoryStatistics() {
        try {
            Map<String, Object> stats = historyService.getHistoryStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to get history statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Clean up old history records for all users
     * @param daysToKeep Number of days to keep (default 90)
     * @return Cleanup result
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldHistory(
            @RequestParam(defaultValue = "90") int daysToKeep) {
        try {
            long totalBefore = historyService.getHistoryCount();
            long deletedCount = historyService.deleteHistoryOlderThan(daysToKeep);
            long totalAfter = historyService.getHistoryCount();
            
            return ResponseEntity.ok(Map.of(
                "message", "History cleanup completed successfully",
                "records_before", totalBefore,
                "records_deleted", deletedCount,
                "records_after", totalAfter,
                "days_kept", daysToKeep,
                "space_saved_percentage", totalBefore > 0 ? (deletedCount * 100.0) / totalBefore : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to cleanup history: " + e.getMessage()));
        }
    }
    
    /**
     * Clean up old history records for a specific user
     * @param userId User ID
     * @param daysToKeep Number of days to keep (default 90)
     * @return Cleanup result
     */
    @DeleteMapping("/user/{userId}/cleanup")
    public ResponseEntity<Map<String, String>> cleanupUserHistory(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "90") int daysToKeep) {
        try {
            historyService.cleanupUserHistory(userId, daysToKeep);
            return ResponseEntity.ok(Map.of(
                "message", "User history cleanup completed successfully",
                "user_id", userId.toString(),
                "days_kept", String.valueOf(daysToKeep)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to cleanup user history: " + e.getMessage()));
        }
    }
    
    /**
     * Optimize history storage (automatic cleanup with default settings)
     * @return Optimization result
     */
    @PostMapping("/optimize")
    public ResponseEntity<Map<String, String>> optimizeHistoryStorage() {
        try {
            long totalBefore = historyService.getHistoryCount();
            historyService.optimizeHistoryStorage();
            long totalAfter = historyService.getHistoryCount();
            long deletedCount = totalBefore - totalAfter;
            
            return ResponseEntity.ok(Map.of(
                "message", "History storage optimized successfully",
                "records_before", String.valueOf(totalBefore),
                "records_deleted", String.valueOf(deletedCount),
                "records_after", String.valueOf(totalAfter),
                "optimization_status", deletedCount > 0 ? "OPTIMIZED" : "NO_CLEANUP_NEEDED"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to optimize history storage: " + e.getMessage()));
        }
    }
    
    // === ORIGINAL ENDPOINTS (kept for backward compatibility) === //
    
    /**
     * Get recent albums only (for debugging/specific use)
     * @param userId User ID
     * @return List of recently played albums
     */
    @GetMapping("/user/{userId}/albums")
    public ResponseEntity<?> getRecentlyPlayedAlbums(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(historyService.getRecentlyPlayedAlbums(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get recent playlists only (for debugging/specific use)
     * @param userId User ID
     * @return List of recently played playlists
     */
    @GetMapping("/user/{userId}/playlists")
    public ResponseEntity<?> getRecentlyPlayedPlaylists(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(historyService.getRecentlyPlayedPlaylists(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get recent artists only (for debugging/specific use)
     * @param userId User ID
     * @return List of recently played artists
     */
    @GetMapping("/user/{userId}/artists")
    public ResponseEntity<?> getRecentlyPlayedArtists(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(historyService.getRecentlyPlayedArtists(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 