package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Response.AlbumSearchDTO;
import org.gb.stellarplayer.DTO.ArtistRadioPlaylistDTO;
import org.gb.stellarplayer.Response.PlaylistSearchDTO;
import org.gb.stellarplayer.Service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationApi {
    
    private final RecommendationService recommendationService;
    
    /**
     * Get recommended albums based on user's listening history and favorite artists
     * @param userId User ID
     * @param limit Number of albums to recommend (default 12)
     * @return List of recommended albums
     */
    @GetMapping("/user/{userId}/albums")
    public ResponseEntity<List<AlbumSearchDTO>> getRecommendedAlbums(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "12") int limit) {
        try {
            List<AlbumSearchDTO> recommendedAlbums = recommendationService.getRecommendedAlbumsBasedOnHistory(userId, limit);
            return ResponseEntity.ok(recommendedAlbums);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all artist radio playlists for a user
     * @param userId User ID
     * @param limit Number of artist radio playlists to return (default 8)
     * @return List of artist radio playlists
     */
    @GetMapping("/user/{userId}/artist-radio")
    public ResponseEntity<List<ArtistRadioPlaylistDTO>> getArtistRadioPlaylists(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "8") int limit) {
        try {
            List<ArtistRadioPlaylistDTO> artistRadioPlaylists = recommendationService.getArtistRadioPlaylists(userId, limit);
            return ResponseEntity.ok(artistRadioPlaylists);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Auto-generate artist radio playlists for a user based on favorites and history
     * @param userId User ID
     * @param limit Number of artist radio playlists to generate (default 8)
     * @return Success message with count
     */
    @PostMapping("/user/{userId}/artist-radio/auto-generate")
    public ResponseEntity<Map<String, Object>> autoGenerateArtistRadioPlaylists(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "8") int limit) {
        try {
            // Get existing count before generation
            List<ArtistRadioPlaylistDTO> before = recommendationService.getArtistRadioPlaylists(userId, limit);
            int existingCount = before.size();
            
            // Force refresh to ensure we have the requested number
            recommendationService.refreshArtistRadioPlaylists(userId);
            
            // Get count after generation
            List<ArtistRadioPlaylistDTO> after = recommendationService.getArtistRadioPlaylists(userId, limit);
            int finalCount = after.size();
            int newlyGenerated = finalCount - existingCount;
            
            return ResponseEntity.ok(Map.of(
                "message", "Artist radio playlists auto-generated successfully",
                "user_id", userId.toString(),
                "existing_playlists", existingCount,
                "total_playlists", finalCount,
                "newly_generated", Math.max(0, newlyGenerated),
                "playlists", after
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to auto-generate artist radio playlists: " + e.getMessage()));
        }
    }
 
    
    /**
     * Get system-wide recommendation playlists (trending, new releases, etc.)
     * @return List of system recommendation playlists
     */
    @GetMapping("/system")
    public ResponseEntity<List<Playlist>> getSystemRecommendations() {
        try {
            List<Playlist> systemPlaylists = recommendationService.getRecommendationPlaylists();
            return ResponseEntity.ok(systemPlaylists);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all user-specific recommendation playlists
     * @param userId User ID
     * @return List of user recommendation playlists
     */
    @GetMapping("/user/{userId}/playlists")
    public ResponseEntity<List<Playlist>> getUserRecommendations(@PathVariable Integer userId) {
        try {
            List<Playlist> userPlaylists = recommendationService.getUserRecommendationPlaylists(userId);
            return ResponseEntity.ok(userPlaylists);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Trigger manual update of system recommendation playlists (admin only)
     * @return Success message
     */
    @PostMapping("/system/update")
    public ResponseEntity<Map<String, String>> updateSystemPlaylists() {
        try {
            recommendationService.updateSystemPlaylists();
            return ResponseEntity.ok(Map.of("message", "System recommendation playlists updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update system playlists: " + e.getMessage()));
        }
    }
    
    /**
     * Trigger manual update of user recommendation playlists
     * @param userId User ID
     * @return Success message
     */
    @PostMapping("/user/{userId}/update")
    public ResponseEntity<Map<String, String>> updateUserPlaylists(@PathVariable Integer userId) {
        try {
            recommendationService.updateUserPlaylists(userId);
            return ResponseEntity.ok(Map.of(
                "message", "User recommendation playlists updated successfully",
                "user_id", userId.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update user playlists: " + e.getMessage()));
        }
    }
} 