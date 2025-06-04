package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Repository.ArtistRepository;
import org.gb.stellarplayer.Service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
public class PlaylistApi {
    @Autowired
    PlaylistService playlistService;

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    @Autowired
    private ArtistRepository artistRepository;

    @GetMapping("/newest")
    public List<Playlist> getAllPlaylists() {
        return playlistService.getPlaylists();
    }

    @GetMapping("/{id}")
    public Playlist getPlaylistById(@PathVariable int id) {
        return playlistService.getPlaylistById(id);
    }

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
}
