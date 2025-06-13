package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Repository.ArtistRepository;
import org.gb.stellarplayer.Service.PlaylistService;
import org.gb.stellarplayer.Service.RecommendationService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.gb.stellarplayer.Model.Enum.PlaylistType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
public class PlaylistApi {
    @Autowired
    PlaylistService playlistService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TrackRepository trackRepository;

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    @Autowired
    private ArtistRepository artistRepository;

    // Default playlist cover
    private static final String DEFAULT_PLAYLIST_COVER = "https://res.cloudinary.com/dll5rlqx9/image/upload/v1749836922/prj-img/playlist-default_u7kf0y.jpg";

    @GetMapping("/newest")
    public List<Playlist> getAllPlaylists() {
        return playlistService.getPlaylists();
    }

    @GetMapping("/{id}")
    public Playlist getPlaylistById(@PathVariable int id) {
        return playlistService.getPlaylistById(id);
    }

    // User playlist management endpoints

    @GetMapping("/user/my")
    public ResponseEntity<List<Playlist>> getUserPlaylists(@RequestHeader("Authorization") String token) {
        try {
            User user = validateAndGetUser(token);
            List<Playlist> userPlaylists = playlistRepository.findByUserIdAndType(user.getId(), PlaylistType.USER);
            return ResponseEntity.ok(userPlaylists);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    @PostMapping("/user/create")
    public ResponseEntity<?> createUserPlaylist(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> requestBody) {
        try {
            User user = validateAndGetUser(token);
            
            Playlist playlist = new Playlist();
            playlist.setName((String) requestBody.get("title"));
            
            // Set cover (use provided cover or default)
            String cover = (String) requestBody.get("cover");
            playlist.setCover(cover != null && !cover.trim().isEmpty() ? cover : DEFAULT_PLAYLIST_COVER);
            
            playlist.setStatus(true); // User playlists are always active
            playlist.setType(PlaylistType.USER);
            playlist.setUser(user);
            playlist.setCreatedAt(LocalDateTime.now());
            playlist.setUpdatedAt(LocalDateTime.now());
            
            Playlist savedPlaylist = playlistRepository.save(playlist);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlaylist);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create playlist: " + e.getMessage()));
        }
    }

    @PutMapping("/user/{playlistId}")
    public ResponseEntity<?> updateUserPlaylist(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer playlistId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            User user = validateAndGetUser(token);
            
            Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Playlist not found"));
            }
            
            Playlist playlist = playlistOpt.get();
            
            // Check ownership
            if (playlist.getUser() == null || !playlist.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only modify your own playlists"));
            }
            
            // Update fields
            if (requestBody.containsKey("title")) {
                playlist.setName((String) requestBody.get("title"));
            }
            if (requestBody.containsKey("cover")) {
                String cover = (String) requestBody.get("cover");
                playlist.setCover(cover != null && !cover.trim().isEmpty() ? cover : DEFAULT_PLAYLIST_COVER);
            }
            
            playlist.setUpdatedAt(LocalDateTime.now());
            
            Playlist updatedPlaylist = playlistRepository.save(playlist);
            return ResponseEntity.ok(updatedPlaylist);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update playlist: " + e.getMessage()));
        }
    }

    @DeleteMapping("/user/{playlistId}")
    public ResponseEntity<?> deleteUserPlaylist(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer playlistId) {
        try {
            User user = validateAndGetUser(token);
            
            Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Playlist not found"));
            }
            
            Playlist playlist = playlistOpt.get();
            
            // Check ownership
            if (playlist.getUser() == null || !playlist.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only delete your own playlists"));
            }
            
            playlistRepository.delete(playlist);
            return ResponseEntity.ok(Map.of("message", "Playlist deleted successfully"));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete playlist: " + e.getMessage()));
        }
    }

    // Track management endpoints

    @PostMapping("/user/{playlistId}/tracks/{trackId}")
    public ResponseEntity<?> addTrackToPlaylist(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer playlistId,
            @PathVariable Integer trackId) {
        try {
            User user = validateAndGetUser(token);
            
            Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Playlist not found"));
            }
            
            Optional<Track> trackOpt = trackRepository.findById(trackId);
            if (trackOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Track not found"));
            }
            
            Playlist playlist = playlistOpt.get();
            Track track = trackOpt.get();
            
            // Check ownership
            if (playlist.getUser() == null || !playlist.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only modify your own playlists"));
            }
            
            // Check if track is already in playlist
            if (playlist.getTracks().contains(track)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Track is already in the playlist"));
            }
            
            playlist.getTracks().add(track);
            playlist.setUpdatedAt(LocalDateTime.now());
            playlistRepository.save(playlist);
            
            return ResponseEntity.ok(Map.of("message", "Track added to playlist successfully"));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to add track to playlist: " + e.getMessage()));
        }
    }

    @DeleteMapping("/user/{playlistId}/tracks/{trackId}")
    public ResponseEntity<?> removeTrackFromPlaylist(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer playlistId,
            @PathVariable Integer trackId) {
        try {
            User user = validateAndGetUser(token);
            
            Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Playlist not found"));
            }
            
            Optional<Track> trackOpt = trackRepository.findById(trackId);
            if (trackOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Track not found"));
            }
            
            Playlist playlist = playlistOpt.get();
            Track track = trackOpt.get();
            
            // Check ownership
            if (playlist.getUser() == null || !playlist.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only modify your own playlists"));
            }
            
            playlist.getTracks().remove(track);
            playlist.setUpdatedAt(LocalDateTime.now());
            playlistRepository.save(playlist);
            
            return ResponseEntity.ok(Map.of("message", "Track removed from playlist successfully"));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to remove track from playlist: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{playlistId}/tracks")
    public ResponseEntity<?> getPlaylistTracks(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer playlistId) {
        try {
            User user = validateAndGetUser(token);
            
            Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
            if (playlistOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Playlist not found"));
            }
            
            Playlist playlist = playlistOpt.get();
            
            // Check if playlist is public or user owns it
            if (playlist.getUser() != null && !playlist.getUser().getId().equals(user.getId()) && !playlist.isStatus()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access denied"));
            }
            
            return ResponseEntity.ok(playlist.getTracks());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to get playlist tracks: " + e.getMessage()));
        }
    }

    // Legacy endpoint (keeping for backward compatibility but without user authentication)
    @PostMapping
    public ResponseEntity<?> addPlaylist(@RequestBody Map<String, Object> requestBody) {
        try {
            Playlist playlist = new Playlist();
            playlist.setName((String) requestBody.get("title"));
            playlist.setCover((String) requestBody.get("cover"));
            playlist.setStatus((Boolean) requestBody.get("status"));
            
            Playlist savedPlaylist = playlistService.addPlaylist(playlist);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlaylist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create playlist: " + e.getMessage()));
        }
    }

    // Recommendation endpoints
    @GetMapping("/recommendations")
    public ResponseEntity<List<Playlist>> getRecommendationPlaylists() {
        List<Playlist> playlists = recommendationService.getRecommendationPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/recommendations/user/{userId}")
    public ResponseEntity<List<Playlist>> getUserRecommendationPlaylists(@PathVariable Integer userId) {
        List<Playlist> playlists = recommendationService.getUserRecommendationPlaylists(userId);
        return ResponseEntity.ok(playlists);
    }

    @PostMapping("/recommendations/generate")
    public ResponseEntity<?> generateSystemPlaylists() {
        try {
            recommendationService.updateSystemPlaylists();
            return ResponseEntity.ok(Map.of("message", "System playlists updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update playlists: " + e.getMessage()));
        }
    }

    @PostMapping("/recommendations/user/{userId}/generate")
    public ResponseEntity<?> generateUserPlaylists(@PathVariable Integer userId) {
        try {
            recommendationService.updateUserPlaylists(userId);
            return ResponseEntity.ok(Map.of("message", "User playlists updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update user playlists: " + e.getMessage()));
        }
    }

    @PostMapping("/recommendations/trending")
    public ResponseEntity<?> generateTrendingPlaylist() {
        try {
            Playlist playlist = recommendationService.generateTrendingWeekly();
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate trending playlist: " + e.getMessage()));
        }
    }

    @PostMapping("/recommendations/playlist/new-daily")
    public ResponseEntity<?> generateNewDailyPlaylist() {
        try {
            Playlist playlist = recommendationService.generateNewMusicDaily();
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate new daily playlist: " + e.getMessage()));
        }
    }

    @PostMapping("/recommendations/playlist/viral")
    public ResponseEntity<?> generateViralPlaylist() {
        try {
            Playlist playlist = recommendationService.generateViralHits();
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate viral playlist: " + e.getMessage()));
        }
    }

    @PostMapping("/recommendations/playlist/new-releases")
    public ResponseEntity<?> generateNewReleasesPlaylist() {
        try {
            Playlist playlist = recommendationService.generateNewReleases();
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate new releases playlist: " + e.getMessage()));
        }
    }

    @PostMapping("/recommendations/user/{userId}/discovery")
    public ResponseEntity<?> generateUserDiscoveryPlaylist(@PathVariable Integer userId) {
        try {
            Playlist playlist = recommendationService.generateUserDiscoveryWeekly(userId);
            if (playlist == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Cannot generate discovery playlist - user has no listening history"));
            }
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate user discovery playlist: " + e.getMessage()));
        }
    }

    /**
     * Validate JWT token and get authenticated user
     * @param token JWT token with "Bearer " prefix
     * @return Authenticated User entity
     * @throws BadRequestException if token is invalid or user not found
     */
    private User validateAndGetUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadRequestException("Invalid token format. Expected 'Bearer <token>'");
        }
        
        String jwt = token.substring(7);
        try {
            jwtUtil.validateJwtToken(jwt);
            String username = jwtUtil.getUserNameFromJwtToken(jwt);
            return userRepository.findByName(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));
        } catch (Exception e) {
            throw new BadRequestException("Invalid JWT token: " + e.getMessage());
        }
    }
}
