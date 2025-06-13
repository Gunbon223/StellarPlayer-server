package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.TrackRequest;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Album management - accessible by admin and artist roles
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/admin/albums")
@RequiredArgsConstructor
public class AlbumMngApi {
    private final AlbumService albumService;
    private final TrackService trackService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Get all albums
     * @param token Authentication token
     * @return List of all albums
     */
    @GetMapping
    public ResponseEntity<List<Album>> getAllAlbums(@RequestHeader("Authorization") String token) {
        validatePermission(token);
        List<Album> albums = albumService.getAlbums();
        return new ResponseEntity<>(albums, HttpStatus.OK);
    }

    /**
     * Get album by ID
     * @param id Album ID
     * @param token Authentication token
     * @return Album details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Album> getAlbumById(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        Album album = albumService.getAlbumById(id);
        return new ResponseEntity<>(album, HttpStatus.OK);
    }

    /**
     * Add a new album
     * @param album Album data
     * @param token Authentication token
     * @return Created album
     */
    @PostMapping
    public ResponseEntity<?> addAlbum(
            @RequestBody Album album,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Album savedAlbum = albumService.addAlbum(album);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create album: " + e.getMessage()));
        }
    }

    /**
     * Update existing album
     * @param album Updated album data
     * @param id Album ID
     * @param token Authentication token
     * @return Updated album
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlbum(
            @RequestBody Album album,
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            album.setId(id);
            Album updatedAlbum = albumService.updateAlbum(album);
            return ResponseEntity.ok(updatedAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update album: " + e.getMessage()));
        }
    }

    /**
     * Delete album
     * This will also clean up all related records (history, favorites, tracks, etc.)
     * @param id Album ID
     * @param token Authentication token
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlbum(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // First verify album exists
            Album album = albumService.getAlbumById(id);
            
            // Delete album with proper cascade handling
            albumService.deleteAlbumWithCascade(id);
            
            return ResponseEntity.ok(Map.of(
                "message", "Album deleted successfully",
                "album_title", album.getTitle(),
                "album_id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete album: " + e.getMessage()));
        }
    }

    /**
     * Get albums by artist ID
     * @param artistId Artist ID
     * @param token Authentication token
     * @return List of albums by the artist
     */
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<Album>> getAlbumsByArtistId(
            @PathVariable int artistId,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            List<Album> albums = albumService.getAlbumsByArtistId(artistId);
            return ResponseEntity.ok(albums);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get artist albums: " + e.getMessage());
        }
    }

    /**
     * Get albums by release date range
     * @param startDate Start date
     * @param endDate End date
     * @param token Authentication token
     * @return List of albums in the date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Album>> getAlbumsByReleaseDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            List<Album> albums = albumService.getAlbumsByReleaseDateRange(
                java.time.LocalDate.parse(startDate),
                java.time.LocalDate.parse(endDate)
            );
            return ResponseEntity.ok(albums);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get albums by date range: " + e.getMessage());
        }
    }

    // TRACK MANAGEMENT ENDPOINTS

    /**
     * Get all tracks in an album
     * @param albumId Album ID
     * @param token Authentication token
     * @return List of tracks in the album as TrackRequest objects
     */
    @GetMapping("/{albumId}/tracks")
    public ResponseEntity<List<TrackRequest>> getAlbumTracks(
            @PathVariable int albumId,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // Verify album exists
            albumService.getAlbumById(albumId);
            List<Track> tracks = trackService.getTrackByAlbumId(albumId);
            
            // Convert Track entities to TrackRequest DTOs
            List<TrackRequest> trackRequests = tracks.stream()
                    .map(this::convertTrackToTrackRequest)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(trackRequests);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get album tracks: " + e.getMessage());
        }
    }

    /**
     * Add a track to an album
     * @param albumId Album ID
     * @param trackId Track ID to add
     * @param token Authentication token
     * @return Updated track
     */
    @PostMapping("/{albumId}/tracks/{trackId}")
    public ResponseEntity<?> addTrackToAlbum(
            @PathVariable int albumId,
            @PathVariable int trackId,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // Verify album exists
            Album album = albumService.getAlbumById(albumId);
            
            // Get the track
            Track track = trackService.getTrackById(trackId);
            
            // Check if track is already in this album
            if (track.getAlbum() != null && track.getAlbum().getId().equals(albumId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Track is already in this album"));
            }
            
            // Set the album for the track
            track.setAlbum(album);
            Track updatedTrack = trackService.updateTrack(track);
            
            return ResponseEntity.ok(updatedTrack);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to add track to album: " + e.getMessage()));
        }
    }

    /**
     * Remove a track from an album
     * @param albumId Album ID
     * @param trackId Track ID to remove
     * @param token Authentication token
     * @return Updated track
     */
    @DeleteMapping("/{albumId}/tracks/{trackId}")
    public ResponseEntity<?> removeTrackFromAlbum(
            @PathVariable int albumId,
            @PathVariable int trackId,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // Verify album exists
            albumService.getAlbumById(albumId);
            
            // Get the track
            Track track = trackService.getTrackById(trackId);
            
            // Check if track is in this album
            if (track.getAlbum() == null || !track.getAlbum().getId().equals(albumId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Track is not in this album"));
            }
            
            // Remove the album from the track
            track.setAlbum(null);
            Track updatedTrack = trackService.updateTrack(track);
            
            return ResponseEntity.ok(updatedTrack);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to remove track from album: " + e.getMessage()));
        }
    }

    /**
     * Debug endpoint for token verification
     * @param token Authentication token
     * @return Token information
     */
    @GetMapping("/token-debug")
    public ResponseEntity<?> debugToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                
                // Check if token is valid
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                boolean isAdmin = jwtUtil.hasAdminRole(jwt);
                boolean isArtist = jwtUtil.hasArtistRole(jwt);
                
                Map<String, Object> debug = Map.of(
                    "username", username,
                    "isAdmin", isAdmin,
                    "isArtist", isArtist,
                    "isAuthorized", isAdmin || isArtist
                );
                return ResponseEntity.ok(debug);
            }
            return ResponseEntity.badRequest().body("Invalid token format");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token validation error: " + e.getMessage());
        }
    }

    /**
     * Validate token and check for admin or artist role
     * @param token JWT token
     * @throws BadRequestException If token is invalid or user does not have required permissions
     */
    private void validatePermission(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                if (!(hasAdminRole(user) || hasArtistRole(user))) {
                    throw new BadRequestException("Access denied. Admin or Artist privileges required");
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
     * @return True if user has admin role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }

    /**
     * Check if user has artist role
     * @param user User entity
     * @return True if user has artist role
     */
    private boolean hasArtistRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ARTIST"));
    }

    /**
     * Helper method to convert Track entity to TrackRequest DTO
     * @param track Track entity
     * @return TrackRequest DTO
     */
    private TrackRequest convertTrackToTrackRequest(Track track) {
        TrackRequest trackRequest = new TrackRequest();
        trackRequest.setTitle(track.getTitle());
        trackRequest.setDuration(track.getDuration());
        trackRequest.setPath(track.getPath());
        trackRequest.setCover(track.getCover());
        trackRequest.setLyrics(track.getLyrics());
        trackRequest.setStatus(track.isStatus());
        trackRequest.setPlayCount(track.getPlayCount() != null ? track.getPlayCount() : 0L);
        trackRequest.setReleaseYear(track.getReleaseYear());
        
        // Set album ID if album exists
        if (track.getAlbum() != null) {
            trackRequest.setAlbumId(track.getAlbum().getId());
        }
        
        // Set artist IDs if artists exist
        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
            List<Integer> artistIds = track.getArtists().stream()
                    .map(artist -> artist.getId())
                    .collect(Collectors.toList());
            trackRequest.setArtist_id(artistIds);
        }
        
        // Set genre (taking the first genre if multiple exist)
        if (track.getGenres() != null && !track.getGenres().isEmpty()) {
            trackRequest.setGenre(track.getGenres().get(0).getName());
        }
        
        return trackRequest;
    }
} 