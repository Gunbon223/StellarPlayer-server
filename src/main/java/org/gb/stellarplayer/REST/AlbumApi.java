package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.PlaylistService;
import org.gb.stellarplayer.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumApi {

    @Autowired
    AlbumService playlistService;
    @Autowired
    TrackService trackService;

    @GetMapping("/newest")
    public List<Album> getAllPlaylists() {
        return playlistService.getAlbums();
    }

    @GetMapping("/{id}")
    public Album getPlaylistById(@PathVariable int id) {
        return playlistService.getAlbumById(id);
    }

    @GetMapping("/{id}/tracks")
    public List<Track> getTracksByAlbumId(@PathVariable int id) {
        return trackService.getTrackByAlbumId(id);
    }



}
