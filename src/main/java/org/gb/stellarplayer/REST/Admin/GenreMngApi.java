package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.DTO.TrackDTO;
import org.gb.stellarplayer.Entites.Genre;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.GenreRequest;
import org.gb.stellarplayer.Service.GenreService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only controller for Genre management
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/v1/admin/genres")
@RequiredArgsConstructor
public class GenreMngApi {
    private final GenreService genreService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Get all genres
     * @param token Admin authentication token
     * @return List of all genres
     */
    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres(@RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        List<Genre> genres = genreService.getAllGenres();
        return new ResponseEntity<>(genres, HttpStatus.OK);
    }

    /**
     * Get genre by ID
     * @param id Genre ID
     * @param token Admin authentication token
     * @return Genre details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        Genre genre = genreService.getGenreById(id);
        return new ResponseEntity<>(genre, HttpStatus.OK);
    }

    /**
     * Create a new genre (admin only)
     * @param genreRequest Genre request data
     * @param token Admin authentication token
     * @return Created genre
     */
    @PostMapping
    public ResponseEntity<?> createGenre(
            @RequestBody GenreRequest genreRequest,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Genre genre = genreService.createGenre(genreRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(genre);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create genre: " + e.getMessage()));
        }
    }

    /**
     * Update an existing genre (admin only)
     * @param genreRequest Updated genre data
     * @param id Genre ID
     * @param token Admin authentication token
     * @return Updated genre
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenre(
            @RequestBody GenreRequest genreRequest,
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Genre genre = genreService.updateGenre(genreRequest, id);
            return ResponseEntity.ok(genre);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update genre: " + e.getMessage()));
        }
    }

    /**
     * Delete a genre (admin only)
     * @param id Genre ID
     * @param token Admin authentication token
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGenre(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            genreService.deleteGenre(id);
            return ResponseEntity.ok(Map.of("message", "Genre deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete genre: " + e.getMessage()));
        }
    }
    
    /**
     * Get track count by genre
     * @param id Genre ID
     * @param token Admin authentication token
     * @return Track count
     */
    @GetMapping("/{id}/track-count")
    public ResponseEntity<?> getTrackCountByGenre(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            long trackCount = genreService.countTracksByGenre(id);
            return ResponseEntity.ok(Map.of(
                    "genre_id", id, 
                    "track_count", trackCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to get track count: " + e.getMessage()));
        }
    }
    
    /**
     * Get genre statistics
     * @param id Genre ID
     * @param token Admin authentication token
     * @return Genre statistics
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getGenreStatistics(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Map<String, Object> statistics = genreService.getGenreStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to get genre statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get tracks by genre
     * @param id Genre ID
     * @param token Admin authentication token
     * @return List of tracks in the genre
     */
    @GetMapping("/{id}/tracks")
    public ResponseEntity<List<TrackDTO>> getTracksByGenre(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        List<Track> tracks = genreService.getTracksByGenre(id);
        List<TrackDTO> trackDTOs = tracks.stream()
                .map(TrackDTO::fromEntity)
                .toList();
        return new ResponseEntity<>(trackDTOs, HttpStatus.OK);
    }
    
    /**
     * Create a playlist from random tracks in a genre
     * @param id Genre ID
     * @param playlistName Name of the playlist (optional)
     * @param trackCount Number of tracks to include (default 20)
     * @param token Admin authentication token
     * @return Created playlist
     */
    @PostMapping("/{id}/create-playlist")
    public ResponseEntity<?> createGenrePlaylist(
            @PathVariable int id,
            @RequestParam(required = false) String playlistName,
            @RequestParam(defaultValue = "20") int trackCount,
            @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Playlist playlist = genreService.createGenrePlaylist(id, playlistName, trackCount);
            return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create playlist: " + e.getMessage()));
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