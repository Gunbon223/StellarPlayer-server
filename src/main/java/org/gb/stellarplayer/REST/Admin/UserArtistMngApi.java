package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.DTO.TrackAdminDTO;
import org.gb.stellarplayer.DTO.UserArtistDTO;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserArtist;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Service.UserArtistService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin API for managing user-artist relationships and track approvals
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/admin/user-artist")
@RequiredArgsConstructor
public class UserArtistMngApi {
    
    private final UserArtistService userArtistService;
    private final TrackService trackService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Link user to artist
     */
    @PostMapping("/link")
    public ResponseEntity<?> linkUserToArtist(
            @RequestParam Integer userId,
            @RequestParam Integer artistId,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            UserArtist userArtist = userArtistService.linkUserToArtist(userId, artistId);
            return ResponseEntity.ok(userArtist);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to link user to artist: " + e.getMessage()));
        }
    }

    /**
     * Unlink user from artist
     */
    @DeleteMapping("/unlink")
    public ResponseEntity<?> unlinkUserFromArtist(
            @RequestParam Integer userId,
            @RequestParam Integer artistId,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            userArtistService.unlinkUserFromArtist(userId, artistId);
            return ResponseEntity.ok(Map.of("message", "User unlinked from artist successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to unlink user from artist: " + e.getMessage()));
        }
    }

    /**
     * Get manageable artists for a user
     */
    @GetMapping("/user/{userId}/artists")
    public ResponseEntity<?> getUserArtists(
            @PathVariable Integer userId,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            List<Artist> artists = userArtistService.getUserArtists(userId);
            return ResponseEntity.ok(artists);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to get user artists: " + e.getMessage()));
        }
    }

    /**
     * Get all user-artist relationships
     */
    @GetMapping("/relationships")
    public ResponseEntity<?> getAllUserArtistRelationships(@RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            List<UserArtistDTO> relationships = userArtistService.getAllUserArtistRelationships();
            return ResponseEntity.ok(relationships);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to get user-artist relationships: " + e.getMessage()));
        }
    }

    /**
     * Get user-artist relationships by user ID
     */
    @GetMapping("/relationships/user/{userId}")
    public ResponseEntity<?> getUserArtistRelationshipsByUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer userId) {
        validateAdminPermission(token);
        try {
            List<UserArtistDTO> relationships = userArtistService.getUserArtistRelationshipsByUserId(userId);
            return ResponseEntity.ok(relationships);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to get user-artist relationships for user: " + e.getMessage()));
        }
    }

    /**
     * Get user-artist relationships by artist ID
     */
    @GetMapping("/relationships/artist/{artistId}")
    public ResponseEntity<?> getUserArtistRelationshipsByArtistId(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer artistId) {
        validateAdminPermission(token);
        try {
            List<UserArtistDTO> relationships = userArtistService.getUserArtistRelationshipsByArtistId(artistId);
            return ResponseEntity.ok(relationships);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to get user-artist relationships for artist: " + e.getMessage()));
        }
    }

    /**
     * Check if user can manage specific artist
     */
    @GetMapping("/user/{userId}/artist/{artistId}/can-manage")
    public ResponseEntity<?> canUserManageArtist(
            @PathVariable Integer userId,
            @PathVariable Integer artistId,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            boolean canManage = userArtistService.canUserManageArtist(userId, artistId);
            return ResponseEntity.ok(Map.of("canManage", canManage));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to check permissions: " + e.getMessage()));
        }
    }

    // TRACK APPROVAL ENDPOINTS

    /**
     * Get all unapproved tracks
     */
    @GetMapping("/tracks/unapproved")
    public ResponseEntity<?> getUnapprovedTracks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
            
            Page<Track> unapprovedTracks = trackService.getUnapprovedTracks(pageable);
            
            List<TrackAdminDTO> trackDTOs = unapprovedTracks.getContent().stream()
                .map(TrackAdminDTO::fromEntity)
                .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                "tracks", trackDTOs,
                "currentPage", unapprovedTracks.getNumber(),
                "totalPages", unapprovedTracks.getTotalPages(),
                "totalElements", unapprovedTracks.getTotalElements(),
                "pageSize", unapprovedTracks.getSize()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to get unapproved tracks: " + e.getMessage()));
        }
    }

    /**
     * Approve a track
     */
    @PostMapping("/tracks/{trackId}/approve")
    public ResponseEntity<?> approveTrack(
            @PathVariable Integer trackId,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            Track track = trackService.getTrackById(trackId);
            track.setStatus(true);
            Track approvedTrack = trackService.updateTrack(track);
            
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(approvedTrack);
            return ResponseEntity.ok(Map.of(
                "message", "Track approved successfully",
                "track", trackDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to approve track: " + e.getMessage()));
        }
    }

    /**
     * Reject/disable a track
     */
    @PostMapping("/tracks/{trackId}/reject")
    public ResponseEntity<?> rejectTrack(
            @PathVariable Integer trackId,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            Track track = trackService.getTrackById(trackId);
            track.setStatus(false);
            Track rejectedTrack = trackService.updateTrack(track);
            
            TrackAdminDTO trackDTO = TrackAdminDTO.fromEntity(rejectedTrack);
            return ResponseEntity.ok(Map.of(
                "message", "Track rejected successfully",
                "track", trackDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to reject track: " + e.getMessage()));
        }
    }

    /**
     * Bulk approve tracks
     */
    @PostMapping("/tracks/bulk-approve")
    public ResponseEntity<?> bulkApproveTracks(
            @RequestBody List<Integer> trackIds,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            List<Track> approvedTracks = trackIds.stream()
                .map(trackId -> {
                    Track track = trackService.getTrackById(trackId);
                    track.setStatus(true);
                    return trackService.updateTrack(track);
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "message", "Tracks approved successfully",
                "approvedCount", approvedTracks.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to bulk approve tracks: " + e.getMessage()));
        }
    }

    /**
     * Bulk reject tracks
     */
    @PostMapping("/tracks/bulk-reject")
    public ResponseEntity<?> bulkRejectTracks(
            @RequestBody List<Integer> trackIds,
            @RequestHeader("Authorization") String token) {
        validateAdminPermission(token);
        try {
            List<Track> rejectedTracks = trackIds.stream()
                .map(trackId -> {
                    Track track = trackService.getTrackById(trackId);
                    track.setStatus(false);
                    return trackService.updateTrack(track);
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "message", "Tracks rejected successfully",
                "rejectedCount", rejectedTracks.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to bulk reject tracks: " + e.getMessage()));
        }
    }

    /**
     * Validate admin permission
     */
    private void validateAdminPermission(String token) {
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

    /**
     * Check if user has admin role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }
} 