package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Service.TrackPlayService;
import org.gb.stellarplayer.Ultils.JwtUtil;
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
    
    @Autowired
    TrackPlayService trackPlayService;
    
    @Autowired
    JwtUtil jwtUtil;
    
    @Autowired
    UserRepository userRepository;
    
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
            stats.put("duration", track.getDuration());
            stats.put("releaseYear", track.getReleaseYear());
            
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
            response.put("releaseYear", track.getReleaseYear());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Track not found");
            error.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Record a play/listen event for a track - AUTHENTICATION REQUIRED
     * This endpoint will increment the play count and handle fraud detection
     * Only authenticated users can record plays
     * @param id Track ID
     * @param listenDuration How long the user listened (in seconds)
     * @param token JWT authorization token
     * @param request HTTP request to get IP address
     * @return Play recording result
     */
    @PostMapping("/{id}/play")
    public ResponseEntity<Map<String, Object>> recordPlay(
            @PathVariable int id,
            @RequestParam(defaultValue = "30") Integer listenDuration,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        try {
            // Validate authentication and get user
            User authenticatedUser = validateAndGetUser(token);
            
            // Get client IP address for fraud detection
            String ipAddress = getClientIpAddress(request);
            
            // Basic validation
            if (listenDuration < 1) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid listen duration");
                error.put("message", "Listen duration must be at least 1 second");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Use TrackPlayService for proper fraud detection and analytics
            // Modified to include user information
            trackPlayService.recordPlay(id, ipAddress, listenDuration, authenticatedUser.getId());
            
            // Get updated track to return current play count
            Track track = trackService.getTrackById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trackId", track.getId());
            response.put("newPlayCount", track.getPlayCount());
            response.put("listenDuration", listenDuration);
            response.put("userId", authenticatedUser.getId());
            response.put("username", authenticatedUser.getName());
            response.put("message", "Play recorded successfully");
            
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(401).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to record play");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Validate JWT token and get authenticated user
     * @param token JWT token with "Bearer " prefix
     * @return Authenticated User entity
     * @throws BadRequestException if token is invalid or user not found
     */
    private User validateAndGetUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadRequestException("Invalid token format. Expected 'Bearer <token>'");
        }
        
        String jwt = token.substring(7);
        try {
            jwtUtil.validateJwtToken(jwt);
            String username = jwtUtil.getUserNameFromJwtToken(jwt);
            return userRepository.findByName(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));
        } catch (Exception e) {
            throw new BadRequestException("Invalid JWT token: " + e.getMessage());
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
