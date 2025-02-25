package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/track")
@RequiredArgsConstructor
public class TrackApi {
    @Autowired
    TrackService trackService;
    @GetMapping("/{id}")
    public Track getTrackById(@PathVariable int id) {
        return trackService.getTrackById(id);
    }
    @GetMapping("/album/{id}")
    public List<Track> getTracksByAlbumId(@PathVariable int id) {
        return trackService.getTrackByAlbumId(id);
    }


}
