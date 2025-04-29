package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Service.FavouriteService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@CrossOrigin
public class FavouriteApi {
    
    private final FavouriteService favouriteService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<?>> getAllUserFavorites(@PathVariable Integer userId,
                                                     @RequestHeader("Authorization") String token) {
        // Validate JWT token
        validateToken(token);
        
        return ResponseEntity.ok(favouriteService.getAllUserFavorites(userId));
    }
    
    @GetMapping("/{userId}/tracks")
    public ResponseEntity<List<Track>> getUserFavoriteTracks(@PathVariable Integer userId,
                                                           @RequestHeader("Authorization") String token) {
        validateToken(token);
        return ResponseEntity.ok(favouriteService.getUserFavoriteTracks(userId));
    }
    
    @GetMapping("/{userId}/playlists")
    public ResponseEntity<List<Playlist>> getUserFavoritePlaylists(@PathVariable Integer userId,
                                                                 @RequestHeader("Authorization") String token) {
        validateToken(token);
        return ResponseEntity.ok(favouriteService.getUserFavoritePlaylists(userId));
    }
    
    @GetMapping("/{userId}/albums")
    public ResponseEntity<List<Album>> getUserFavoriteAlbums(@PathVariable Integer userId,
                                                           @RequestHeader("Authorization") String token) {
        validateToken(token);
        return ResponseEntity.ok(favouriteService.getUserFavoriteAlbums(userId));
    }
    
    @GetMapping("/{userId}/artists")
    public ResponseEntity<List<Artist>> getUserFavoriteArtists(@PathVariable Integer userId,
                                                             @RequestHeader("Authorization") String token) {
        validateToken(token);
        return ResponseEntity.ok(favouriteService.getUserFavoriteArtists(userId));
    }
    
    // Check if item is in favorites
    @GetMapping("/{userId}/tracks/{trackId}/check")
    public ResponseEntity<Map<String, Boolean>> checkIfTrackInFavorites(@PathVariable Integer userId,
                                                                     @PathVariable Integer trackId,
                                                                     @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean isInFavorites = favouriteService.isTrackInFavorites(userId, trackId);
        return ResponseEntity.ok(Map.of("isFavorite", isInFavorites));
    }
    
    @GetMapping("/{userId}/playlists/{playlistId}/check")
    public ResponseEntity<Map<String, Boolean>> checkIfPlaylistInFavorites(@PathVariable Integer userId,
                                                                         @PathVariable Integer playlistId,
                                                                         @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean isInFavorites = favouriteService.isPlaylistInFavorites(userId, playlistId);
        return ResponseEntity.ok(Map.of("isFavorite", isInFavorites));
    }
    
    @GetMapping("/{userId}/albums/{albumId}/check")
    public ResponseEntity<Map<String, Boolean>> checkIfAlbumInFavorites(@PathVariable Integer userId,
                                                                      @PathVariable Integer albumId,
                                                                      @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean isInFavorites = favouriteService.isAlbumInFavorites(userId, albumId);
        return ResponseEntity.ok(Map.of("isFavorite", isInFavorites));
    }
    
    @GetMapping("/{userId}/artists/{artistId}/check")
    public ResponseEntity<Map<String, Boolean>> checkIfArtistInFavorites(@PathVariable Integer userId,
                                                                       @PathVariable Integer artistId,
                                                                       @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean isInFavorites = favouriteService.isArtistInFavorites(userId, artistId);
        return ResponseEntity.ok(Map.of("isFavorite", isInFavorites));
    }
    
    // Add to favorites
    @PostMapping("/{userId}/tracks/{trackId}")
    public ResponseEntity<Map<String, String>> addTrackToFavorites(@PathVariable Integer userId,
                                                                @PathVariable Integer trackId,
                                                                @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean added = favouriteService.addTrackToFavorites(userId, trackId);
        if (added) {
            return ResponseEntity.ok(Map.of("message", "Track added to favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Track already in favorites"));
        }
    }
    
    @PostMapping("/{userId}/playlists/{playlistId}")
    public ResponseEntity<Map<String, String>> addPlaylistToFavorites(@PathVariable Integer userId,
                                                                    @PathVariable Integer playlistId,
                                                                    @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean added = favouriteService.addPlaylistToFavorites(userId, playlistId);
        if (added) {
            return ResponseEntity.ok(Map.of("message", "Playlist added to favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Playlist already in favorites"));
        }
    }
    
    @PostMapping("/{userId}/albums/{albumId}")
    public ResponseEntity<Map<String, String>> addAlbumToFavorites(@PathVariable Integer userId,
                                                                 @PathVariable Integer albumId,
                                                                 @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean added = favouriteService.addAlbumToFavorites(userId, albumId);
        if (added) {
            return ResponseEntity.ok(Map.of("message", "Album added to favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Album already in favorites"));
        }
    }
    
    @PostMapping("/{userId}/artists/{artistId}")
    public ResponseEntity<Map<String, String>> addArtistToFavorites(@PathVariable Integer userId,
                                                                  @PathVariable Integer artistId,
                                                                  @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean added = favouriteService.addArtistToFavorites(userId, artistId);
        if (added) {
            return ResponseEntity.ok(Map.of("message", "Artist added to favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Artist already in favorites"));
        }
    }
    
    // Remove from favorites
    @DeleteMapping("/{userId}/tracks/{trackId}")
    public ResponseEntity<Map<String, String>> removeTrackFromFavorites(@PathVariable Integer userId,
                                                                     @PathVariable Integer trackId,
                                                                     @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean removed = favouriteService.removeTrackFromFavorites(userId, trackId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Track removed from favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Track was not in favorites"));
        }
    }
    
    @DeleteMapping("/{userId}/playlists/{playlistId}")
    public ResponseEntity<Map<String, String>> removePlaylistFromFavorites(@PathVariable Integer userId,
                                                                         @PathVariable Integer playlistId,
                                                                         @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean removed = favouriteService.removePlaylistFromFavorites(userId, playlistId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Playlist removed from favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Playlist was not in favorites"));
        }
    }
    
    @DeleteMapping("/{userId}/albums/{albumId}")
    public ResponseEntity<Map<String, String>> removeAlbumFromFavorites(@PathVariable Integer userId,
                                                                      @PathVariable Integer albumId,
                                                                      @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean removed = favouriteService.removeAlbumFromFavorites(userId, albumId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Album removed from favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Album was not in favorites"));
        }
    }
    
    @DeleteMapping("/{userId}/artists/{artistId}")
    public ResponseEntity<Map<String, String>> removeArtistFromFavorites(@PathVariable Integer userId,
                                                                       @PathVariable Integer artistId,
                                                                       @RequestHeader("Authorization") String token) {
        validateToken(token);
        boolean removed = favouriteService.removeArtistFromFavorites(userId, artistId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Artist removed from favorites"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("message", "Artist was not in favorites"));
        }
    }
    
    // Helper method to validate JWT token
    private void validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            jwtUtil.validateJwtToken(jwt);
        } else {
            throw new RuntimeException("Invalid or missing JWT token");
        }
    }
}
