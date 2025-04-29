package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Request.TrackRequest;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/track")
@RequiredArgsConstructor
public class TrackApi {
    @Autowired
    TrackService trackService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/{id}")
    public Track getTrackById(@PathVariable int id) {
        return trackService.getTrackById(id);
    }
    
    @GetMapping("/album/{id}")
    public List<Track> getTracksByAlbumId(@PathVariable int id) {
        return trackService.getTrackByAlbumId(id);
    }

    @PostMapping
    public ResponseEntity<?> addTrack(@RequestBody TrackRequest trackRequest, 
                                     @RequestHeader("Authorization") String token) {
        // Validate token and check admin role
        if (!validateAdminToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access denied. Admin privileges required"));
        }
        
        try {
            Track savedTrack = trackService.saveTrack(convertRequestToTrack(trackRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrack);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to create track: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrack(@PathVariable int id, 
                                        @RequestBody TrackRequest trackRequest,
                                        @RequestHeader("Authorization") String token) {
        // Validate token and check admin role
        if (!validateAdminToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access denied. Admin privileges required"));
        }
        
        try {
            // First check if track exists
            Track existingTrack = trackService.getTrackById(id);
            
            // Convert request to track and set id
            Track trackToUpdate = convertRequestToTrack(trackRequest);
            trackToUpdate.setId(id);
            
            // Update track
            Track updatedTrack = trackService.updateTrack(trackToUpdate);
            return ResponseEntity.ok(updatedTrack);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to update track: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrack(@PathVariable int id,
                                        @RequestHeader("Authorization") String token) {
        // Validate token and check admin role
        if (!validateAdminToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access denied. Admin privileges required"));
        }
        
        try {
            trackService.deleteTrack(id);
            return ResponseEntity.ok(Map.of("message", "Track deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to delete track: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to validate token and check for admin role
     */
    private boolean validateAdminToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                return jwtUtil.hasAdminRole(jwt);
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        }
        throw new BadRequestException("Invalid token format");
    }
    
    /**
     * Helper method to convert TrackRequest to Track entity
     */
    private Track convertRequestToTrack(TrackRequest trackRequest) {
        // This is a simplified conversion - in a real application,
        // you would need to fetch artists, album, etc. based on IDs
        Track track = new Track();
        track.setTitle(trackRequest.getTitle());
        track.setDuration(trackRequest.getDuration());
        track.setPath(trackRequest.getPath());
        track.setCover(trackRequest.getCover());
        // Additional fields would need to be set based on your requirements
        
        return track;
    }
}
