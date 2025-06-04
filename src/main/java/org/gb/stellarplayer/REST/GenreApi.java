package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Genre;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Service.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public API controller for Genre operations (read-only)
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreApi {
    private final GenreService genreService;

    /**
     * Get all genres
     * @return List of all genres
     */
    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        List<Genre> genres = genreService.getAllGenres();
        return new ResponseEntity<>(genres, HttpStatus.OK);
    }

    /**
     * Get genre by ID
     * @param id Genre ID
     * @return Genre details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable int id) {
        Genre genre = genreService.getGenreById(id);
        return new ResponseEntity<>(genre, HttpStatus.OK);
    }
    
    /**
     * Get track count by genre
     * @param id Genre ID
     * @return Track count
     */
    @GetMapping("/{id}/track-count")
    public ResponseEntity<?> getTrackCountByGenre(@PathVariable int id) {
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
     * @return Genre statistics
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getGenreStatistics(@PathVariable int id) {
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
     * @return List of tracks in the genre
     */
    @GetMapping("/{id}/tracks")
    public ResponseEntity<List<Track>> getTracksByGenre(@PathVariable int id) {
        List<Track> tracks = genreService.getTracksByGenre(id);
        return new ResponseEntity<>(tracks, HttpStatus.OK);
    }
    
    /**
     * Create a playlist from random tracks in a genre
     * @param id Genre ID
     * @param playlistName Name of the playlist (optional)
     * @param trackCount Number of tracks to include (default 20)
     * @return Created playlist
     */
    @PostMapping("/{id}/create-playlist")
    public ResponseEntity<?> createGenrePlaylist(
            @PathVariable int id,
            @RequestParam(required = false) String playlistName,
            @RequestParam(defaultValue = "20") int trackCount) {
        try {
            Playlist playlist = genreService.createGenrePlaylist(id, playlistName, trackCount);
            return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create playlist: " + e.getMessage()));
        }
    }
} 