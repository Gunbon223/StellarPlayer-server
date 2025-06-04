package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.ArtistService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/artist")
@RequiredArgsConstructor
public class ArtistApi {
    private final ArtistService artistService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<Artist> getAllArtists() {
        return artistService.getArtists();
    }

    @GetMapping("/{id}")
    public Artist getArtistById(@PathVariable int id) {
        return artistService.getArtistById(id);
    }

    @GetMapping("/{id}/tracks")
    public ResponseEntity<Page<Track>> getTracksByArtistId(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "desc") String sort) {
        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(Sort.Direction.fromString(sort), "createdAt"));
        Page<Track> tracks = artistService.getTracksByArtistId(id, pageRequest);
        return ResponseEntity.ok(tracks);
    }

    @GetMapping("/{id}/albums")
    public ResponseEntity<Page<Album>> getAlbumsByArtistId(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "desc") String sort) {
        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(Sort.Direction.fromString(sort), "createdAt"));
        Page<Album> albums = artistService.getAlbumsByArtistId(id, pageRequest);
        return ResponseEntity.ok(albums);
    }

    @PostMapping
    public ResponseEntity<?> addArtist(@RequestBody Artist artist, @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            Artist savedArtist = artistService.addArtist(artist);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedArtist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create artist: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArtist(@PathVariable int id, @RequestBody Artist artist, @RequestHeader("Authorization") String token) {
        validateAdminOrArtistToken(token, id);
        try {
            artist.setId(id);
            Artist updatedArtist = artistService.updateArtist(artist);
            return ResponseEntity.ok(updatedArtist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update artist: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArtist(@PathVariable int id, @RequestHeader("Authorization") String token) {
        validateAdminToken(token);
        try {
            artistService.deleteArtist(id);
            return ResponseEntity.ok(Map.of("message", "Artist deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete artist: " + e.getMessage()));
        }
    }

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

    private void validateAdminOrArtistToken(String token, int artistId) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                if (!hasAdminRole(user) && !isArtistAndOwnsProfile(user, artistId)) {
                    throw new BadRequestException("Access denied. Admin or Artist privileges required");
                }
            } catch (Exception e) {     
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
    }

    private boolean isArtistAndOwnsProfile(User user, int artistId) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_ARTIST"));
    }
} 