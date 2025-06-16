package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.DTO.TrackAdminDTO;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.TrackRequest;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Service.UserArtistService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API for artists to manage their own content
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/artist")
@RequiredArgsConstructor
public class ArtistContentApi {
    
    private final TrackService trackService;
    private final AlbumService albumService;
    private final UserArtistService userArtistService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ARTIST MANAGEMENT
    
    /**
     * Get all artists managed by the current user
     */
    @GetMapping("/my-artists")
    public ResponseEntity<?> getMyArtists(@RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            List<Artist> artists = userArtistService.getUserArtists(user.getId());
            return ResponseEntity.ok(artists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get artists: " + e.getMessage()));
        }
    }

    // TRACK MANAGEMENT

    /**
     * Get all tracks for artists managed by the current user
     */
    @GetMapping("/my-tracks")
    public ResponseEntity<?> getMyTracks(@RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            List<Artist> myArtists = userArtistService.getUserArtists(user.getId());
            List<Track> allTracks = myArtists.stream()
                .flatMap(artist -> trackService.getTracksByArtistId(artist.getId()).stream())
                .collect(Collectors.toList());
            
            List<TrackAdminDTO> trackDTOs = allTracks.stream()
                .map(TrackAdminDTO::fromEntity)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(trackDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get tracks: " + e.getMessage()));
        }
    }

    /**
     * Get track by ID (if user owns it)
     */
    @GetMapping("/track/{id}")
    public ResponseEntity<?> getTrack(@PathVariable int id, @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageTrack(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to access this track"));
            }
            
            Track track = trackService.getTrackById(id);
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(track);
            return ResponseEntity.ok(trackDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get track: " + e.getMessage()));
        }
    }

    /**
     * Update track (if user owns it)
     */
    @PutMapping("/track/{id}")
    public ResponseEntity<?> updateTrack(
            @PathVariable int id,
            @RequestBody TrackRequest trackRequest,
            @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageTrack(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to update this track"));
            }
            
            Track existingTrack = trackService.getTrackById(id);
            Track trackToUpdate = convertRequestToTrack(trackRequest);
            trackToUpdate.setId(id);
            
            Track updatedTrack = trackService.updateTrack(trackToUpdate);
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(updatedTrack);
            return ResponseEntity.ok(trackDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to update track: " + e.getMessage()));
        }
    }

    /**
     * Update track status only (if user owns it)
     * @param id Track ID
     * @param status New status value
     * @param token Authentication token
     * @return Updated track
     */
    @PutMapping("/track/{id}/status")
    public ResponseEntity<?> updateTrackStatus(
            @PathVariable int id,
            @RequestParam boolean status,
            @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageTrack(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to update this track"));
            }
            
            // Get existing track
            Track existingTrack = trackService.getTrackById(id);
            
            // Update only the status field
            existingTrack.setStatus(status);
            
            // Save the updated track
            Track updatedTrack = trackService.updateTrack(existingTrack);
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(updatedTrack);
            
            return ResponseEntity.ok(Map.of(
                "message", "Track status updated successfully",
                "track", trackDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to update track status: " + e.getMessage()));
        }
    }

    /**
     * Delete track (if user owns it)
     */
    @DeleteMapping("/track/{id}")
    public ResponseEntity<?> deleteTrack(@PathVariable int id, @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageTrack(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to delete this track"));
            }
            
            // Get track details before deletion for response
            Track track = trackService.getTrackById(id);
            String trackTitle = track.getTitle();
            
            // Use cascade deletion to properly clean up related records
            trackService.deleteTrackWithCascade(id);
            
            return ResponseEntity.ok(Map.of(
                "message", "Track deleted successfully with all related data",
                "track_title", trackTitle,
                "track_id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to delete track: " + e.getMessage()));
        }
    }

    // ALBUM MANAGEMENT

    /**
     * Get all albums for artists managed by the current user
     */
    @GetMapping("/my-albums")
    public ResponseEntity<?> getMyAlbums(@RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            List<Artist> myArtists = userArtistService.getUserArtists(user.getId());
            List<Album> allAlbums = myArtists.stream()
                .flatMap(artist -> albumService.getAlbumsByArtistId(artist.getId()).stream())
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(allAlbums);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get albums: " + e.getMessage()));
        }
    }

    /**
     * Get album by ID (if user owns it)
     */
    @GetMapping("/album/{id}")
    public ResponseEntity<?> getAlbum(@PathVariable int id, @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageAlbum(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to access this album"));
            }
            
            Album album = albumService.getAlbumById(id);
            return ResponseEntity.ok(album);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get album: " + e.getMessage()));
        }
    }

    /**
     * Create new album
     */
    @PostMapping("/album")
    public ResponseEntity<?> createAlbum(@RequestBody Album album, @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            // Verify user can manage all artists in the album
            if (album.getArtists() != null && !album.getArtists().isEmpty()) {
                for (Artist artist : album.getArtists()) {
                    if (!userArtistService.canUserManageArtist(user.getId(), artist.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("message", "You don't have permission to create album for artist: " + artist.getName()));
                    }
                }
            }
            
            Album savedAlbum = albumService.addAlbum(album);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to create album: " + e.getMessage()));
        }
    }

    /**
     * Update album (if user owns it)
     */
    @PutMapping("/album/{id}")
    public ResponseEntity<?> updateAlbum(
            @PathVariable int id,
            @RequestBody Album album,
            @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageAlbum(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to update this album"));
            }
            
            album.setId(id);
            Album updatedAlbum = albumService.updateAlbum(album);
            return ResponseEntity.ok(updatedAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to update album: " + e.getMessage()));
        }
    }

    /**
     * Delete album (if user owns it)
     */
    @DeleteMapping("/album/{id}")
    public ResponseEntity<?> deleteAlbum(@PathVariable int id, @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageAlbum(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to delete this album"));
            }
            
            Album album = albumService.getAlbumById(id);
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
     * Add track to album (if user owns both)
     */
    @PostMapping("/album/{albumId}/track/{trackId}")
    public ResponseEntity<?> addTrackToAlbum(
            @PathVariable int albumId,
            @PathVariable int trackId,
            @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageAlbum(user.getId(), albumId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to manage this album"));
            }
            
            if (!userArtistService.canUserManageTrack(user.getId(), trackId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to manage this track"));
            }
            
            Album album = albumService.getAlbumById(albumId);
            Track track = trackService.getTrackById(trackId);
            
            if (track.getAlbum() != null && track.getAlbum().getId().equals(albumId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Track is already in this album"));
            }
            
            track.setAlbum(album);
            Track updatedTrack = trackService.updateTrack(track);
            
            return ResponseEntity.ok(updatedTrack);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to add track to album: " + e.getMessage()));
        }
    }

    /**
     * Remove track from album (if user owns both)
     */
    @DeleteMapping("/album/{albumId}/track/{trackId}")
    public ResponseEntity<?> removeTrackFromAlbum(
            @PathVariable int albumId,
            @PathVariable int trackId,
            @RequestHeader("Authorization") String token) {
        User user = validateArtistToken(token);
        try {
            if (!userArtistService.canUserManageAlbum(user.getId(), albumId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to manage this album"));
            }
            
            if (!userArtistService.canUserManageTrack(user.getId(), trackId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You don't have permission to manage this track"));
            }
            
            Track track = trackService.getTrackById(trackId);
            
            if (track.getAlbum() == null || !track.getAlbum().getId().equals(albumId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Track is not in this album"));
            }
            
            track.setAlbum(null);
            Track updatedTrack = trackService.updateTrack(track);
            
            return ResponseEntity.ok(updatedTrack);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to remove track from album: " + e.getMessage()));
        }
    }

    /**
     * Validate token and return user with artist role
     */
    private User validateArtistToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                if (!hasArtistRole(user)) {
                    throw new BadRequestException("Access denied. Artist privileges required");
                }
                return user;
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    /**
     * Check if user has artist role
     */
    private boolean hasArtistRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ARTIST"));
    }

    /**
     * Helper method to convert TrackRequest to Track entity
     */
    private Track convertRequestToTrack(TrackRequest trackRequest) {
        Track track = new Track();
        track.setTitle(trackRequest.getTitle());
        track.setDuration(trackRequest.getDuration());
        track.setPath(trackRequest.getPath());
        track.setCover(trackRequest.getCover());
        track.setLyrics(trackRequest.getLyrics());
        track.setStatus(trackRequest.isStatus());
        track.setPlayCount(trackRequest.getPlayCount());
        track.setReleaseYear(trackRequest.getReleaseYear());
        return track;
    }
} 