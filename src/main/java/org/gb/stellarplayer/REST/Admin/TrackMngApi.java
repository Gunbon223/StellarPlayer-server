package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.DTO.TrackAdminDTO;
import org.gb.stellarplayer.DTO.TrackStatsDTO;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.TrackRequest;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Service.TrackStatsService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for Track management - accessible by admin and artist roles
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/admin/track")
@RequiredArgsConstructor
public class TrackMngApi {
    private final TrackService trackService;
    private final TrackStatsService trackStatsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Get track by ID
     * @param id Track ID
     * @param token Authentication token
     * @return Track details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrack(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Track track = trackService.getTrackById(id);
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(track);
            return ResponseEntity.ok(trackDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get track: " + e.getMessage()));
        }
    }
    
    /**
     * Get all tracks with pagination and sorting
     * @param page Page number (zero-based)
     * @param pageSize Number of items per page
     * @param sortOrder Sort direction ("asc" or "desc")
     * @param sortBy Field to sort by (id, title, artistName)
     * @param token Authentication token
     * @return Paginated list of tracks
     */
    @GetMapping
    public ResponseEntity<?> getAllTracks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pagenumber", defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // Convert sort parameters
            boolean isAscending = "asc".equalsIgnoreCase(sortOrder);
            
            // Get paginated tracks
            Map<String, Object> paginatedResult = trackService.getPaginatedTracks(
                page, pageSize, sortBy, isAscending);
                
            return ResponseEntity.ok(paginatedResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get tracks: " + e.getMessage()));
        }
    }
    
    /**
     * Create a new track
     * @param trackRequest Track request data
     * @param token Authentication token
     * @return Created track
     */
    @PostMapping
    public ResponseEntity<?> createTrack(
            @RequestBody TrackRequest trackRequest, 
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Track savedTrack = trackService.saveTrack(convertRequestToTrack(trackRequest));
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(savedTrack);
            return ResponseEntity.status(HttpStatus.CREATED).body(trackDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to create track: " + e.getMessage()));
        }
    }

    /**
     * Update an existing track
     * @param trackRequest Updated track data
     * @param id Track ID
     * @param token Authentication token
     * @return Updated track
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrack(
            @RequestBody TrackRequest trackRequest,
            @PathVariable int id, 
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // First check if track exists
            Track existingTrack = trackService.getTrackById(id);
            
            // Convert request to track and set id
            Track trackToUpdate = convertRequestToTrack(trackRequest);
            trackToUpdate.setId(id);
            
            // Update track
            Track updatedTrack = trackService.updateTrack(trackToUpdate);
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(updatedTrack);
            return ResponseEntity.ok(trackDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to update track: " + e.getMessage()));
        }
    }

    /**
     * Update track status only (separate endpoint to avoid wiping other fields)
     * @param id Track ID
     * @param status New status value
     * @param token Authentication token
     * @return Updated track
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTrackStatus(
            @PathVariable int id,
            @RequestParam boolean status,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // Get existing track
            Track existingTrack = trackService.getTrackById(id);
            
            // Update only the status field
            existingTrack.setStatus(status);
            
            // Save the updated track
            Track updatedTrack = trackService.updateTrack(existingTrack);
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(updatedTrack);
            
            return ResponseEntity.ok(Map.of(
                "message", "Track status updated successfully",
                "track", trackDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to update track status: " + e.getMessage()));
        }
    }

    /**
     * Delete a track
     * @param id Track ID
     * @param token Authentication token
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrack(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // Get track details before deletion for response
            Track track = trackService.getTrackById(id);
            String trackTitle = track.getTitle();
            
            // Use cascade deletion to properly clean up related records
            trackService.deleteTrackWithCascade(id);
            
            return ResponseEntity.ok(Map.of(
                "message", "Track deleted successfully with all related data",
                "track_title", trackTitle,
                "track_id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to delete track: " + e.getMessage()));
        }
    }
    
    // TRACK STATISTICS ENDPOINTS
    
    /**
     * Get basic track statistics
     * @param id Track ID
     * @param token Authentication token
     * @return Track statistics
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getTrackStats(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Track track = trackService.getTrackById(id);
            TrackStatsDTO statsDTO = TrackStatsDTO.basicStatsFromTrack(track);
            return ResponseEntity.ok(statsDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get track statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get detailed track statistics
     * @param id Track ID
     * @param startDate Optional start date for the range
     * @param endDate Optional end date for the range
     * @param token Authentication token
     * @return Detailed track statistics
     */
    @GetMapping("/{id}/stats/detailed")
    public ResponseEntity<?> getDetailedTrackStats(
            @PathVariable int id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Map<String, Object> detailedStats = trackStatsService.getDetailedTrackStats(id, startDate, endDate);
            return ResponseEntity.ok(detailedStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get detailed track statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get weekly track statistics
     * @param id Track ID
     * @param startDate Optional start date for the week
     * @param token Authentication token
     * @return Weekly track statistics
     */
    @GetMapping("/{id}/stats/weekly")
    public ResponseEntity<?> getWeeklyTrackStats(
            @PathVariable int id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Map<String, Long> weeklyStats = trackStatsService.getWeeklyPlayCounts(id, startDate);
            return ResponseEntity.ok(weeklyStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get weekly track statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get monthly track statistics
     * @param id Track ID
     * @param year Optional year (defaults to current year)
     * @param token Authentication token
     * @return Monthly track statistics
     */
    @GetMapping("/{id}/stats/monthly")
    public ResponseEntity<?> getMonthlyTrackStats(
            @PathVariable int id,
            @RequestParam(required = false) Integer year,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Map<String, Long> monthlyStats = trackStatsService.getMonthlyPlayCounts(id, year);
            return ResponseEntity.ok(monthlyStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get monthly track statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get track engagement statistics
     * @param id Track ID
     * @param token Authentication token
     * @return Track engagement statistics
     */
    @GetMapping("/{id}/stats/engagement")
    public ResponseEntity<?> getTrackEngagementStats(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Map<String, Object> engagementStats = trackStatsService.getTrackEngagementStats(id);
            return ResponseEntity.ok(engagementStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to get track engagement statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Debug endpoint for token verification
     * @param token Authentication token
     * @return Token information
     */
    @GetMapping("/token-debug")
    public ResponseEntity<?> debugToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                
                // Check if token is valid
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                boolean isAdmin = jwtUtil.hasAdminRole(jwt);
                boolean isArtist = jwtUtil.hasArtistRole(jwt);
                
                Map<String, Object> debug = Map.of(
                    "username", username,
                    "isAdmin", isAdmin,
                    "isArtist", isArtist,
                    "isAuthorized", isAdmin || isArtist
                );
                return ResponseEntity.ok(debug);
            }
            return ResponseEntity.badRequest().body("Invalid token format");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token validation error: " + e.getMessage());
        }
    }
    
    /**
     * Validate token and check for admin or artist role
     * @param token JWT token
     * @throws BadRequestException If token is invalid or user does not have required permissions
     */
    private void validatePermission(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                if (!(hasAdminRole(user) || hasArtistRole(user))) {
                    throw new BadRequestException("Access denied. Admin or Artist privileges required");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }
    
    /**
     * Check if user has admin role
     * @param user User entity
     * @return True if user has admin role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }
    
    /** 8
     * Check if user has artist role
     * @param user User entityzsxdc dxcfs vxdcfv cv gbv b
     * @return True if user has artist role
     */
    private boolean hasArtistRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ARTIST"));
    }
    
    /**
     * Helper method to convert TrackRequest to Track entity
     * @param trackRequest Track request data
     * @return Track entity
     */
    private Track convertRequestToTrack(TrackRequest trackRequest) {
        Track track = new Track();
        track.setTitle(trackRequest.getTitle());
        track.setDuration(trackRequest.getDuration());
        track.setPath(trackRequest.getPath());
        track.setCover(trackRequest.getCover());
        track.setLyrics(trackRequest.getLyrics());
        track.setStatus(trackRequest.isStatus());
        track.setPlayCount(trackRequest.getPlayCount());
        track.setReleaseYear(trackRequest.getReleaseYear());
        
        // Additional fields would need to be set based on your requirements
        
        return track;
    }
} 