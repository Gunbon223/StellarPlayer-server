package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
public class PlaylistApi {
    @Autowired
    PlaylistService playlistService;

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @GetMapping("/newest")
    public List<Playlist> getAllPlaylists() {
        return playlistService.getPlaylists();
    }

    @GetMapping("/{id}")
    public Playlist getPlaylistById(@PathVariable int id) {
        return playlistService.getPlaylistById(id);
    }

}
