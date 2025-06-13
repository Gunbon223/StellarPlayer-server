package org.gb.stellarplayer.Api;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Service.CloudinaryService;
import org.gb.stellarplayer.Service.PlaylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/playlist-covers")
@RequiredArgsConstructor
public class PlaylistCoverApi {
    
    private final PlaylistService playlistService;
    private final CloudinaryService cloudinaryService;
    
    /**
     * Generate a playlist cover with text overlay using Cloudinary transformations
     */
    @PostMapping("/generate/{playlistId}")
    public ResponseEntity<Map<String, String>> generatePlaylistCover(@PathVariable Integer playlistId) {
        try {
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            if (playlist == null) {
                return ResponseEntity.notFound().build();
            }
            
            String coverUrl = generateCoverWithTextOverlay(playlist);
            
            // Update playlist cover
            playlist.setCover(coverUrl);
            playlistService.updatePlaylist(playlist);
            
            Map<String, String> response = new HashMap<>();
            response.put("coverUrl", coverUrl);
            response.put("message", "Playlist cover generated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate cover: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get optimized cover URL for a playlist type with text overlay
     */
    @GetMapping("/preview/{playlistId}")
    public ResponseEntity<Map<String, String>> getPlaylistCoverPreview(@PathVariable Integer playlistId) {
        try {
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            if (playlist == null) {
                return ResponseEntity.notFound().build();
            }
            
            String coverUrl = generateCoverWithTextOverlay(playlist);
            
            Map<String, String> response = new HashMap<>();
            response.put("coverUrl", coverUrl);
            response.put("playlistName", playlist.getName());
            response.put("playlistType", playlist.getType().name());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to preview cover: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Generate cover URL with text overlay using Cloudinary transformations
     */
    private String generateCoverWithTextOverlay(Playlist playlist) {
        String baseImageUrl = getBaseImageUrl(playlist);
        String primaryText = getPrimaryText(playlist);
        String secondaryText = getSecondaryText(playlist);
        
        // Use Cloudinary transformation to add text overlay
        return buildCloudinaryCoverUrl(baseImageUrl, primaryText, secondaryText, playlist.getType().name());
    }
    
    /**
     * Get base image from first track or default
     */
    private String getBaseImageUrl(Playlist playlist) {
        if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {
            String firstTrackCover = playlist.getTracks().get(0).getCover();
            if (firstTrackCover != null && !firstTrackCover.isEmpty()) {
                return firstTrackCover;
            }
            // Try album cover
            if (playlist.getTracks().get(0).getAlbum() != null) {
                String albumCover = playlist.getTracks().get(0).getAlbum().getCover();
                if (albumCover != null && !albumCover.isEmpty()) {
                    return albumCover;
                }
            }
        }
        
        // Return default image based on playlist type
        return getDefaultImageByType(playlist.getType().name());
    }
    
    /**
     * Get primary text for overlay
     */
    private String getPrimaryText(Playlist playlist) {
        switch (playlist.getType()) {
            case TRENDING:
                return "Trending";
            case NEW_DAILY:
                return "Daily Mix";
            case NEW_RELEASE:
                return "New Releases";
            case VIRAL:
                return "Viral Hits";
            case USER_REC:
            case DISCOVERY:
                return "Discover Weekly";
            case GENRE_MIX:
                return "Genre Mix";
            case ARTIST_MIX:
                return "Artist Mix";
            default:
                return playlist.getName();
        }
    }
    
    /**
     * Get secondary text for overlay
     */
    private String getSecondaryText(Playlist playlist) {
        switch (playlist.getType()) {
            case TRENDING:
                return "This Week";
            case NEW_DAILY:
                return "Updated Daily";
            case NEW_RELEASE:
                return "Fresh Music";
            case VIRAL:
                return "Hot Right Now";
            case USER_REC:
            case DISCOVERY:
                return "Made for You";
            case GENRE_MIX:
                return "Your Favorites";
            case ARTIST_MIX:
                return "Your Artists";
            default:
                return "";
        }
    }
    
    /**
     * Build Cloudinary URL with text overlay transformations
     */
    private String buildCloudinaryCoverUrl(String baseImageUrl, String primaryText, String secondaryText, String playlistType) {
        // Extract public_id from Cloudinary URL if it's a Cloudinary image
        String publicId = extractPublicIdFromUrl(baseImageUrl);
        
        if (publicId == null) {
            // Use default image if base image is not from Cloudinary
            publicId = "defaults/default_playlist_cover";
        }
        
        // Build transformation URL with text overlays
        StringBuilder transformations = new StringBuilder();
        transformations.append("c_fill,w_640,h_640,q_auto,f_auto/"); // Resize and optimize
        
        // Add gradient overlay for better text readability
        transformations.append("l_overlay:gradient_overlay,o_60/");
        
        // Add primary text
        transformations.append("l_text:Arial_48_bold:").append(encodeText(primaryText))
                      .append(",co_white,g_south_west,x_20,y_60/");
        
        // Add secondary text if available
        if (secondaryText != null && !secondaryText.isEmpty()) {
            transformations.append("l_text:Arial_24:").append(encodeText(secondaryText))
                          .append(",co_white,g_south_west,x_20,y_25/");
        }
        
        // Add playlist type badge for certain types
        if ("NEW_DAILY".equals(playlistType)) {
            transformations.append("l_overlay:badge_01,g_north_east,x_20,y_20/");
        }
        
        return "https://res.cloudinary.com/dll5rlqx9/image/upload/" + transformations.toString() + publicId + ".jpg";
    }
    
    /**
     * Extract public_id from Cloudinary URL
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Extract public_id from URL pattern
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String afterUpload = parts[1];
                // Remove version and transformations if present
                if (afterUpload.startsWith("v")) {
                    int slashIndex = afterUpload.indexOf('/', 1);
                    if (slashIndex > 0) {
                        afterUpload = afterUpload.substring(slashIndex + 1);
                    }
                }
                // Remove file extension
                int dotIndex = afterUpload.lastIndexOf('.');
                if (dotIndex > 0) {
                    afterUpload = afterUpload.substring(0, dotIndex);
                }
                return afterUpload;
            }
        } catch (Exception e) {
            // If extraction fails, return null to use default
        }
        
        return null;
    }
    
    /**
     * Encode text for Cloudinary URL
     */
    private String encodeText(String text) {
        return text.replace(" ", "%20").replace(",", "%2C");
    }
    
    /**
     * Get default image public_id by playlist type
     */
    private String getDefaultImageByType(String type) {
        switch (type) {
            case "TRENDING":
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/trending_base.jpg";
            case "NEW_DAILY":
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/daily_mix_base.jpg";
            case "NEW_RELEASE":
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/new_releases_base.jpg";
            case "VIRAL":
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/viral_base.jpg";
            case "USER_REC":
            case "DISCOVERY":
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/discover_weekly_base.jpg";
            case "GENRE_MIX":
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/genre_mix_base.jpg";
            case "ARTIST_MIX":
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/artist_mix_base.jpg";
            default:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/defaults/default_playlist_base.jpg";
        }
    }
} 