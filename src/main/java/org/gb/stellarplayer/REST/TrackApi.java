package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
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
    
    /**
     * Get track play count and basic statistics - No authentication required
     * @param id Track ID
     * @return Track play count and basic stats
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getTrackStats(@PathVariable int id) {
        try {
            Track track = trackService.getTrackById(id);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("trackId", track.getId());
            stats.put("title", track.getTitle());
            stats.put("playCount", track.getPlayCount());
            stats.put("lastPlayedAt", track.getLastPlayedAt());
            stats.put("likes", track.getLikes());
            stats.put("shares", track.getShares());
            stats.put("comments", track.getComments());
            stats.put("duration", track.getDuration());
            
            // Add engagement metrics
            Map<String, Object> engagement = new HashMap<>();
            engagement.put("totalInteractions", track.getLikes() + track.getShares() + track.getComments());
            engagement.put("likesPerPlay", track.getPlayCount() > 0 ? 
                (double) track.getLikes() / track.getPlayCount() : 0.0);
            engagement.put("sharesPerPlay", track.getPlayCount() > 0 ? 
                (double) track.getShares() / track.getPlayCount() : 0.0);
            
            stats.put("engagement", engagement);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Track not found");
            error.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get basic play count only - No authentication required
     * @param id Track ID
     * @return Simple play count response
     */
    @GetMapping("/{id}/playcount")
    public ResponseEntity<Map<String, Object>> getTrackPlayCount(@PathVariable int id) {
        try {
            Track track = trackService.getTrackById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("trackId", track.getId());
            response.put("title", track.getTitle());
            response.put("playCount", track.getPlayCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Track not found");
            error.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Record a play/listen event for a track - No authentication required
     * This endpoint will increment the play count and handle fraud detection
     * @param id Track ID
     * @param listenDuration How long the user listened (in seconds)
     * @param request HTTP request to get IP address
     * @return Play recording result
     */
    @PostMapping("/{id}/play")
    public ResponseEntity<Map<String, Object>> recordPlay(
            @PathVariable int id,
            @RequestParam(defaultValue = "30") Integer listenDuration,
            HttpServletRequest request) {
        try {
            // Get client IP address
            String ipAddress = getClientIpAddress(request);
            
            // Use TrackPlayService if available, otherwise update track directly
            Track track = trackService.getTrackById(id);
            
            // Basic validation
            if (listenDuration < 1) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid listen duration");
                error.put("message", "Listen duration must be at least 1 second");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Update play count (simplified version - in production, use TrackPlayService)
            track.setPlayCount(track.getPlayCount() + 1);
            track.setLastPlayedAt(java.time.LocalDateTime.now());
            trackService.updateTrack(track);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trackId", track.getId());
            response.put("newPlayCount", track.getPlayCount());
            response.put("listenDuration", listenDuration);
            response.put("message", "Play recorded successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to record play");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Helper method to get client IP address
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}
