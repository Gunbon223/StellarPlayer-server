package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.ArtistRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumApi {

    @Autowired
    AlbumService albumService;
    @Autowired
    TrackService trackService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ArtistRepository artistRepository;

    @GetMapping("/newest")
    public List<Album> getAllAlbums() {
        return albumService.getAlbums();
    }

    @GetMapping("/{id}")
    public Album getAlbumById(@PathVariable int id) {
        return albumService.getAlbumById(id);
    }

    @GetMapping("/{id}/tracks")
    public List<Track> getTracksByAlbumId(@PathVariable int id) {
        return trackService.getTrackByAlbumId(id);
    }

    @PostMapping
    public ResponseEntity<?> addAlbum(@RequestBody Map<String, Object> requestBody, @RequestHeader("Authorization") String token) {
        validateArtistOrAdminToken(token);
        try {
            Album album = new Album();
            album.setTitle((String) requestBody.get("title"));
            album.setCover((String) requestBody.get("cover"));
            album.setStatus((Boolean) requestBody.get("status"));
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> artistsList = (List<Map<String, Object>>) requestBody.get("artists");
            if (artistsList != null) {
                List<Artist> artists = artistsList.stream()
                    .map(artistMap -> {
                        Integer artistId = (Integer) artistMap.get("id");
                        return artistRepository.findById(artistId)
                            .orElseThrow(() -> new BadRequestException("Artist not found with id: " + artistId));
                    })
                    .collect(Collectors.toList());
                album.setArtists(artists);
            }
            
            Album savedAlbum = albumService.addAlbum(album);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create album: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlbum(@PathVariable int id, @RequestBody Album album, @RequestHeader("Authorization") String token) {
        validateArtistOrAdminToken(token);
        try {
            album.setId(id);
            Album updatedAlbum = albumService.updateAlbum(album);
            return ResponseEntity.ok(updatedAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update album: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlbum(@PathVariable int id, @RequestHeader("Authorization") String token) {
        validateArtistOrAdminToken(token);
        try {
            albumService.deleteAlbum(id);
            return ResponseEntity.ok(Map.of("message", "Album deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete album: " + e.getMessage()));
        }
    }

    private void validateArtistOrAdminToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                if (!hasArtistOrAdminRole(user)) {
                    throw new BadRequestException("Access denied. Artist or Admin privileges required");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    private boolean hasArtistOrAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN") || role.getName().name().equals("ARTIST"));
    }
}
