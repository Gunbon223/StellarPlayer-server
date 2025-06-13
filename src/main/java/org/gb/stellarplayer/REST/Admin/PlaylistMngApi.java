package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.PlaylistService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only controller for playlist management
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/v1/admin/playlists")
@RequiredArgsConstructor
public class PlaylistMngApi {
    private final PlaylistService playlistService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Get all playlists
     * @param token Admin authentication token
     * @return List of all playlists
     */
    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        List<Playlist> playlists = playlistService.getPlaylists();
        return new ResponseEntity<>(playlists, HttpStatus.OK);
    }

    /**
     * Get playlist by ID
     * @param id Playlist ID
     * @param token Admin authentication token
     * @return Playlist details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        Playlist playlist = playlistService.getPlaylistById(id);
        return new ResponseEntity<>(playlist, HttpStatus.OK);
    }

    /**
     * Add a new playlist (admin only)
     * @param playlist Playlist data
     * @param token Admin authentication token
     * @return Created playlist
     */
    @PostMapping
    public ResponseEntity<?> addPlaylist(
            @RequestBody Playlist playlist,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Playlist savedPlaylist = playlistService.addPlaylist(playlist);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlaylist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create playlist: " + e.getMessage()));
        }
    }

    /**
     * Update existing playlist (admin only)
     * @param playlist Updated playlist data
     * @param id Playlist ID
     * @param token Admin authentication token
     * @return Updated playlist
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlaylist(
            @RequestBody Playlist playlist,
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            playlist.setId(id);
            Playlist updatedPlaylist = playlistService.updatePlaylist(playlist);
            return ResponseEntity.ok(updatedPlaylist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update playlist: " + e.getMessage()));
        }
    }

    /**
     * Delete playlist (admin only)
     * This will also clean up all related records (history, favorites, etc.)
     * @param id Playlist ID
     * @param token Admin authentication token
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaylist(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            // First verify playlist exists
            Playlist playlist = playlistService.getPlaylistById(id);
            
            // Delete playlist with proper cascade handling
            playlistService.deletePlaylistWithCascade(id);
            
            return ResponseEntity.ok(Map.of(
                "message", "Playlist deleted successfully", 
                "playlist_name", playlist.getName(),
                "playlist_id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete playlist: " + e.getMessage()));
        }
    }

    /**
     * Get all tracks in a playlist
     * @param playlistId Playlist ID
     * @param token Admin authentication token
     * @return List of tracks in the playlist
     */
    @GetMapping("/{playlistId}/tracks")
    public ResponseEntity<List<Track>> getPlaylistTracks(
            @PathVariable int playlistId,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            List<Track> tracks = playlistService.getTracksByPlaylistId(playlistId);
            return ResponseEntity.ok(tracks);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get playlist tracks: " + e.getMessage());
        }
    }

    /**
     * Add a track to a playlist
     * @param playlistId Playlist ID
     * @param trackId Track ID to add
     * @param token Admin authentication token
     * @return Updated playlist
     */
    @PostMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<?> addTrackToPlaylist(
            @PathVariable int playlistId,
            @PathVariable int trackId,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Playlist updatedPlaylist = playlistService.addTrackToPlaylist(playlistId, trackId);
            return ResponseEntity.ok(updatedPlaylist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to add track to playlist: " + e.getMessage()));
        }
    }

    /**
     * Remove a track from a playlist
     * @param playlistId Playlist ID
     * @param trackId Track ID to remove
     * @param token Admin authentication token
     * @return Updated playlist
     */
    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<?> removeTrackFromPlaylist(
            @PathVariable int playlistId,
            @PathVariable int trackId,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Playlist updatedPlaylist = playlistService.removeTrackFromPlaylist(playlistId, trackId);
            return ResponseEntity.ok(updatedPlaylist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to remove track from playlist: " + e.getMessage()));
        }
    }

    /**
     * Get track count for a playlist
     * @param playlistId Playlist ID
     * @param token Admin authentication token
     * @return Track count information
     */
    @GetMapping("/{playlistId}/track-count")
    public ResponseEntity<?> getPlaylistTrackCount(
            @PathVariable int playlistId,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            List<Track> tracks = playlistService.getTracksByPlaylistId(playlistId);
            
            return ResponseEntity.ok(Map.of(
                    "playlist_id", playlistId,
                    "playlist_name", playlist.getName(),
                    "track_count", tracks.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to get playlist track count: " + e.getMessage()));
        }
    }

    /**
     * Get playlist statistics
     * @param playlistId Playlist ID
     * @param token Admin authentication token
     * @return Playlist statistics
     */
    @GetMapping("/{playlistId}/statistics")
    public ResponseEntity<?> getPlaylistStatistics(
            @PathVariable int playlistId,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            List<Track> tracks = playlistService.getTracksByPlaylistId(playlistId);
            
            // Calculate total duration
            int totalDuration = tracks.stream()
                    .mapToInt(Track::getDuration)
                    .sum();
            
            // Count unique artists
            long uniqueArtistsCount = tracks.stream()
                    .flatMap(track -> track.getArtists().stream())
                    .distinct()
                    .count();
            
            Map<String, Object> statistics = Map.of(
                    "playlist_id", playlistId,
                    "playlist_name", playlist.getName(),
                    "playlist_type", playlist.getType().name(),
                    "track_count", tracks.size(),
                    "total_duration_seconds", totalDuration,
                    "total_duration_formatted", formatDuration(totalDuration),
                    "unique_artists_count", uniqueArtistsCount,
                    "created_at", playlist.getCreatedAt(),
                    "updated_at", playlist.getUpdatedAt()
            );
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to get playlist statistics: " + e.getMessage()));
        }
    }

    /**
     * Format duration in seconds to a human-readable format (HH:MM:SS)
     * @param durationInSeconds Duration in seconds
     * @return Formatted duration string
     */
    private String formatDuration(int durationInSeconds) {
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    /**
     * Validate admin token
     * @param token JWT token
     * @throws BadRequestException If token is invalid or user is not an admin
     */
    private void validateAdminToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                if (!hasAdminRole(user)) {
                    throw new BadRequestException("Access denied. Admin privileges required");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    /**
     * Check if user has admin role
     * @param user User entity
     * @return True if user has admin role, false otherwise
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }
} 