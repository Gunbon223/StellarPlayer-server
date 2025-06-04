package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.DTO.FavouriteTrackDTO;
import org.gb.stellarplayer.DTO.FavouriteAlbumDTO;
import org.gb.stellarplayer.DTO.FavouriteArtistDTO;
import org.gb.stellarplayer.DTO.FavouritePlaylistDTO;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.FavouriteService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for User Favourites management - accessible by authenticated users
 * Users can only access their own favourites unless they have admin role
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
@Log4j2
public class FavouriteApi {
    
    private final FavouriteService favouriteService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // TRACK FAVOURITES
    
    /**
     * Get user's favourite tracks
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return List of favourite tracks as FavouriteTrackDTO with favourite date
     */
    @GetMapping("/tracks")
    public ResponseEntity<?> getUserFavouriteTracks(
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            List<FavouriteTrackDTO> favouriteTracks = favouriteService.getUserFavouriteTrackEntities(targetUserId);
            return ResponseEntity.ok(Map.of(
                "tracks", favouriteTracks,
                "count", favouriteTracks.size()
            ));
        } catch (Exception e) {
            log.error("Error getting favourite tracks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get favourite tracks: " + e.getMessage()));
        }
    }

    /**
     * Add track to favourites
     * @param trackId Track ID
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return Success message
     */
    @PostMapping("/tracks/{trackId}")
    public ResponseEntity<?> addTrackToFavourites(
            @PathVariable Integer trackId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            UserFavouriteTrack favourite = favouriteService.addTrackToFavourites(targetUserId, trackId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "message", "Track added to favourites successfully",
                    "favourite", favourite
                ));
        } catch (Exception e) {
            log.error("Error adding track to favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to add track to favourites: " + e.getMessage()));
        }
    }

    /**
     * Remove track from favourites
     * @param trackId Track ID
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return Success message
     */
    @DeleteMapping("/tracks/{trackId}")
    public ResponseEntity<?> removeTrackFromFavourites(
            @PathVariable Integer trackId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            favouriteService.removeTrackFromFavourites(targetUserId, trackId);
            return ResponseEntity.ok(Map.of("message", "Track removed from favourites successfully"));
        } catch (Exception e) {
            log.error("Error removing track from favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to remove track from favourites: " + e.getMessage()));
        }
    }

    /**
     * Check if track is in favourites
     * @param trackId Track ID
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return Boolean indicating if track is favourite
     */
    @GetMapping("/tracks/{trackId}/check")
    public ResponseEntity<?> isTrackFavourite(
            @PathVariable Integer trackId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean isFavourite = favouriteService.isTrackFavourite(targetUserId, trackId);
            return ResponseEntity.ok(Map.of("isFavourite", isFavourite));
        } catch (Exception e) {
            log.error("Error checking track favourite status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to check track favourite status: " + e.getMessage()));
        }
    }

    /**
     * Get user's favourite tracks with pagination
     * @param page Page number (zero-based)
     * @param pageSize Number of items per page
     * @param sortOrder Sort direction ("asc" or "desc")
     * @param sortBy Field to sort by (id, title, created date)
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return Paginated list of favourite tracks as FavouriteTrackDTO with favourite date
     */
    @GetMapping("/tracks/paginated")
    public ResponseEntity<?> getUserFavouriteTracksPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean ascending = "asc".equalsIgnoreCase(sortOrder);
            
            Map<String, Object> result = favouriteService.getUserFavouriteTracksPaginated(
                targetUserId, page, pageSize, sortBy, ascending);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting paginated favourite tracks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get paginated favourite tracks: " + e.getMessage()));
        }
    }

    // ALBUM FAVOURITES
    
    @GetMapping("/albums")
    public ResponseEntity<?> getUserFavouriteAlbums(
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            List<FavouriteAlbumDTO> favouriteAlbums = favouriteService.getUserFavouriteAlbumEntities(targetUserId);
            return ResponseEntity.ok(Map.of(
                "albums", favouriteAlbums,
                "count", favouriteAlbums.size()
            ));
        } catch (Exception e) {
            log.error("Error getting favourite albums: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get favourite albums: " + e.getMessage()));
        }
    }

    @PostMapping("/albums/{albumId}")
    public ResponseEntity<?> addAlbumToFavourites(
            @PathVariable Integer albumId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            UserFavouriteAlbum favourite = favouriteService.addAlbumToFavourites(targetUserId, albumId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "message", "Album added to favourites successfully",
                    "favourite", favourite
                ));
        } catch (Exception e) {
            log.error("Error adding album to favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to add album to favourites: " + e.getMessage()));
        }
    }

    @DeleteMapping("/albums/{albumId}")
    public ResponseEntity<?> removeAlbumFromFavourites(
            @PathVariable Integer albumId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            favouriteService.removeAlbumFromFavourites(targetUserId, albumId);
            return ResponseEntity.ok(Map.of("message", "Album removed from favourites successfully"));
        } catch (Exception e) {
            log.error("Error removing album from favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to remove album from favourites: " + e.getMessage()));
        }
    }

    @GetMapping("/albums/{albumId}/check")
    public ResponseEntity<?> isAlbumFavourite(
            @PathVariable Integer albumId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean isFavourite = favouriteService.isAlbumFavourite(targetUserId, albumId);
            return ResponseEntity.ok(Map.of("isFavourite", isFavourite));
        } catch (Exception e) {
            log.error("Error checking album favourite status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to check album favourite status: " + e.getMessage()));
        }
    }

    /**
     * Get user's favourite albums with pagination
     * @param page Page number (zero-based)
     * @param pageSize Number of items per page
     * @param sortOrder Sort direction ("asc" or "desc")
     * @param sortBy Field to sort by (id, title, created date)
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return Paginated list of favourite albums
     */
    @GetMapping("/albums/paginated")
    public ResponseEntity<?> getUserFavouriteAlbumsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean ascending = "asc".equalsIgnoreCase(sortOrder);
            
            Map<String, Object> result = favouriteService.getUserFavouriteAlbumsPaginated(
                targetUserId, page, pageSize, sortBy, ascending);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting paginated favourite albums: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get paginated favourite albums: " + e.getMessage()));
        }
    }

    // ARTIST FAVOURITES
    
    @GetMapping("/artists")
    public ResponseEntity<?> getUserFavouriteArtists(
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            List<FavouriteArtistDTO> favouriteArtists = favouriteService.getUserFavouriteArtistEntities(targetUserId);
            return ResponseEntity.ok(Map.of(
                "artists", favouriteArtists,
                "count", favouriteArtists.size()
            ));
        } catch (Exception e) {
            log.error("Error getting favourite artists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get favourite artists: " + e.getMessage()));
        }
    }

    @PostMapping("/artists/{artistId}")
    public ResponseEntity<?> addArtistToFavourites(
            @PathVariable Integer artistId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            UserFavouriteArtist favourite = favouriteService.addArtistToFavourites(targetUserId, artistId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "message", "Artist added to favourites successfully",
                    "favourite", favourite
                ));
        } catch (Exception e) {
            log.error("Error adding artist to favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to add artist to favourites: " + e.getMessage()));
        }
    }

    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<?> removeArtistFromFavourites(
            @PathVariable Integer artistId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            favouriteService.removeArtistFromFavourites(targetUserId, artistId);
            return ResponseEntity.ok(Map.of("message", "Artist removed from favourites successfully"));
        } catch (Exception e) {
            log.error("Error removing artist from favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to remove artist from favourites: " + e.getMessage()));
        }
    }

    @GetMapping("/artists/{artistId}/check")
    public ResponseEntity<?> isArtistFavourite(
            @PathVariable Integer artistId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean isFavourite = favouriteService.isArtistFavourite(targetUserId, artistId);
            return ResponseEntity.ok(Map.of("isFavourite", isFavourite));
        } catch (Exception e) {
            log.error("Error checking artist favourite status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to check artist favourite status: " + e.getMessage()));
        }
    }

    /**
     * Get user's favourite artists with pagination
     * @param page Page number (zero-based)
     * @param pageSize Number of items per page
     * @param sortOrder Sort direction ("asc" or "desc")
     * @param sortBy Field to sort by (id, title, created date)
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return Paginated list of favourite artists
     */
    @GetMapping("/artists/paginated")
    public ResponseEntity<?> getUserFavouriteArtistsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean ascending = "asc".equalsIgnoreCase(sortOrder);
            
            Map<String, Object> result = favouriteService.getUserFavouriteArtistsPaginated(
                targetUserId, page, pageSize, sortBy, ascending);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting paginated favourite artists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get paginated favourite artists: " + e.getMessage()));
        }
    }

    // PLAYLIST FAVOURITES
    
    @GetMapping("/playlists")
    public ResponseEntity<?> getUserFavouritePlaylists(
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            List<FavouritePlaylistDTO> favouritePlaylists = favouriteService.getUserFavouritePlaylistEntities(targetUserId);
            return ResponseEntity.ok(Map.of(
                "playlists", favouritePlaylists,
                "count", favouritePlaylists.size()
            ));
        } catch (Exception e) {
            log.error("Error getting favourite playlists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get favourite playlists: " + e.getMessage()));
        }
    }

    @PostMapping("/playlists/{playlistId}")
    public ResponseEntity<?> addPlaylistToFavourites(
            @PathVariable Integer playlistId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            UserFavouritePlaylist favourite = favouriteService.addPlaylistToFavourites(targetUserId, playlistId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "message", "Playlist added to favourites successfully",
                    "favourite", favourite
                ));
        } catch (Exception e) {
            log.error("Error adding playlist to favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to add playlist to favourites: " + e.getMessage()));
        }
    }

    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<?> removePlaylistFromFavourites(
            @PathVariable Integer playlistId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            favouriteService.removePlaylistFromFavourites(targetUserId, playlistId);
            return ResponseEntity.ok(Map.of("message", "Playlist removed from favourites successfully"));
        } catch (Exception e) {
            log.error("Error removing playlist from favourites: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to remove playlist from favourites: " + e.getMessage()));
        }
    }

    @GetMapping("/playlists/{playlistId}/check")
    public ResponseEntity<?> isPlaylistFavourite(
            @PathVariable Integer playlistId,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean isFavourite = favouriteService.isPlaylistFavourite(targetUserId, playlistId);
            return ResponseEntity.ok(Map.of("isFavourite", isFavourite));
        } catch (Exception e) {
            log.error("Error checking playlist favourite status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to check playlist favourite status: " + e.getMessage()));
        }
    }

    /**
     * Get user's favourite playlists with pagination
     * @param page Page number (zero-based)
     * @param pageSize Number of items per page
     * @param sortOrder Sort direction ("asc" or "desc")
     * @param sortBy Field to sort by (id, title, created date)
     * @param userId User ID (optional - defaults to authenticated user)
     * @param token Authentication token
     * @return Paginated list of favourite playlists
     */
    @GetMapping("/playlists/paginated")
    public ResponseEntity<?> getUserFavouritePlaylistsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(required = false) Integer userId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer targetUserId = validateAndGetUserId(token, userId);
            boolean ascending = "asc".equalsIgnoreCase(sortOrder);
            
            Map<String, Object> result = favouriteService.getUserFavouritePlaylistsPaginated(
                targetUserId, page, pageSize, sortBy, ascending);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting paginated favourite playlists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get paginated favourite playlists: " + e.getMessage()));
        }
    }

    // STATISTICS ENDPOINTS (Admin only)
    
    @GetMapping("/stats/tracks/{trackId}")
    public ResponseEntity<?> getTrackFavouriteCount(
            @PathVariable Integer trackId,
            @RequestHeader("Authorization") String token) {
        try {
            validateAdminPermission(token);
            long count = favouriteService.getTrackFavouriteCount(trackId);
            return ResponseEntity.ok(Map.of("trackId", trackId, "favouriteCount", count));
        } catch (Exception e) {
            log.error("Error getting track favourite count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get track favourite count: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/albums/{albumId}")
    public ResponseEntity<?> getAlbumFavouriteCount(
            @PathVariable Integer albumId,
            @RequestHeader("Authorization") String token) {
        try {
            validateAdminPermission(token);
            long count = favouriteService.getAlbumFavouriteCount(albumId);
            return ResponseEntity.ok(Map.of("albumId", albumId, "favouriteCount", count));
        } catch (Exception e) {
            log.error("Error getting album favourite count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get album favourite count: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/artists/{artistId}")
    public ResponseEntity<?> getArtistFavouriteCount(
            @PathVariable Integer artistId,
            @RequestHeader("Authorization") String token) {
        try {
            validateAdminPermission(token);
            long count = favouriteService.getArtistFavouriteCount(artistId);
            return ResponseEntity.ok(Map.of("artistId", artistId, "favouriteCount", count));
        } catch (Exception e) {
            log.error("Error getting artist favourite count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get artist favourite count: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/playlists/{playlistId}")
    public ResponseEntity<?> getPlaylistFavouriteCount(
            @PathVariable Integer playlistId,
            @RequestHeader("Authorization") String token) {
        try {
            validateAdminPermission(token);
            long count = favouriteService.getPlaylistFavouriteCount(playlistId);
            return ResponseEntity.ok(Map.of("playlistId", playlistId, "favouriteCount", count));
        } catch (Exception e) {
            log.error("Error getting playlist favourite count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get playlist favourite count: " + e.getMessage()));
        }
    }

    // UTILITY METHODS
    
    /**
     * Validate token and get user ID
     * If userId is provided and user is admin, allow access to any user's data
     * Otherwise, return the authenticated user's ID
     */
    private Integer validateAndGetUserId(String token, Integer requestedUserId) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadRequestException("Invalid token format");
        }
        
        String jwt = token.substring(7);
        try {
            jwtUtil.validateJwtToken(jwt);
            String username = jwtUtil.getUserNameFromJwtToken(jwt);
            User authenticatedUser = userRepository.findByName(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));
            
            // If no specific userId requested, return authenticated user's ID
            if (requestedUserId == null) {
                return authenticatedUser.getId();
            }
            
            // If requesting own data, allow
            if (requestedUserId.equals(authenticatedUser.getId())) {
                return requestedUserId;
            }
            
            // If requesting other user's data, check admin permission
            if (hasAdminRole(authenticatedUser)) {
                return requestedUserId;
            }
            
            throw new BadRequestException("Access denied. You can only access your own favourites");
            
        } catch (Exception e) {
            throw new BadRequestException("Invalid JWT token: " + e.getMessage());
        }
    }
    
    /**
     * Validate admin permission
     */
    private void validateAdminPermission(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadRequestException("Invalid token format");
        }
        
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
    }
    
    /**
     * Check if user has admin role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }
}
