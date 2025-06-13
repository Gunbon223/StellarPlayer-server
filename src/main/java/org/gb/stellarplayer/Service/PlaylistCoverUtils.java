package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Model.Enum.PlaylistType;

public class PlaylistCoverUtils {
    
    /**
     * Set playlist cover from first track or default based on type
     */
    public static void setPlaylistCover(Playlist playlist) {
        if (playlist == null) return;
        
        String coverUrl = getFirstTrackCover(playlist);
        
        if (coverUrl == null || coverUrl.isEmpty()) {
            coverUrl = getDefaultCoverByType(playlist.getType());
        }
        
        playlist.setCover(coverUrl);
    }
    
    /**
     * Get cover URL from first track in playlist
     */
    public static String getFirstTrackCover(Playlist playlist) {
        if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {
            Track firstTrack = playlist.getTracks().get(0);
            
            if (firstTrack.getCover() != null && !firstTrack.getCover().isEmpty()) {
                return firstTrack.getCover();
            }
            
            if (firstTrack.getAlbum() != null && 
                firstTrack.getAlbum().getCover() != null && 
                !firstTrack.getAlbum().getCover().isEmpty()) {
                return firstTrack.getAlbum().getCover();
            }
        }
        return null;
    }
    
    /**
     * Generate Cloudinary URL with text overlay for playlist covers
     */
    public static String generateCoverWithText(String baseImageUrl, PlaylistType type) {
        if (baseImageUrl == null || baseImageUrl.isEmpty()) {
            return getDefaultCoverByType(type);
        }
        
        String primaryText = getPrimaryText(type);
        String secondaryText = getSecondaryText(type);
        
        // If it's not a Cloudinary URL, return the original
        if (!baseImageUrl.contains("cloudinary.com")) {
            return baseImageUrl;
        }
        
        // Extract public_id and build transformation URL
        String publicId = extractPublicIdFromCloudinaryUrl(baseImageUrl);
        if (publicId != null) {
            return buildCloudinaryTransformationUrl(publicId, primaryText, secondaryText, type);
        }
        
        return baseImageUrl;
    }
    
    /**
     * Get default cover URL based on playlist type
     */
    public static String getDefaultCoverByType(PlaylistType type) {
        String baseUrl = "https://res.cloudinary.com/dll5rlqx9/image/upload/";
        
        switch (type) {
            case TRENDING:
                return baseUrl + "c_fill,w_640,h_640,q_auto/l_text:Arial_48_bold:Trending,co_white,g_south_west,x_20,y_60/l_text:Arial_24:This%20Week,co_white,g_south_west,x_20,y_25/trending.jpg";
            
            case NEW_DAILY:
                return baseUrl + "c_fill,w_640,h_640,q_auto/l_text:Arial_48_bold:Daily%20Mix,co_white,g_south_west,x_20,y_60/l_text:Arial_24:Updated%20Daily,co_white,g_south_west,x_20,y_25/l_overlay:badge_01,g_north_east,x_20,y_20/defaults/daily_mix_bg.jpg";
            
            case NEW_RELEASE:
                return baseUrl + "c_fill,w_640,h_640,q_auto/l_text:Arial_48_bold:New%20Releases,co_white,g_south_west,x_20,y_60/l_text:Arial_24:Fresh%20Music,co_white,g_south_west,x_20,y_25/defaults/new_releases_bg.jpg";
            
            case VIRAL:
                return baseUrl + "c_fill,w_640,h_640,q_auto/l_text:Arial_48_bold:Viral%20Hits,co_white,g_south_west,x_20,y_60/l_text:Arial_24:Hot%20Right%20Now,co_white,g_south_west,x_20,y_25/defaults/viral_bg.jpg";
            
            case USER_REC:
            case DISCOVERY:
                return baseUrl + "c_fill,w_640,h_640,q_auto/l_text:Arial_48_bold:Discover%20Weekly,co_white,g_south_west,x_20,y_60/l_text:Arial_24:Made%20for%20You,co_white,g_south_west,x_20,y_25/defaults/discover_bg.jpg";
            
            case GENRE_MIX:
                return baseUrl + "c_fill,w_640,h_640,q_auto/l_text:Arial_48_bold:Genre%20Mix,co_white,g_south_west,x_20,y_60/l_text:Arial_24:Your%20Favorites,co_white,g_south_west,x_20,y_25/defaults/genre_mix_bg.jpg";
            
            case ARTIST_MIX:
                return baseUrl + "c_fill,w_640,h_640,q_auto/l_text:Arial_48_bold:Artist%20Mix,co_white,g_south_west,x_20,y_60/l_text:Arial_24:Your%20Artists,co_white,g_south_west,x_20,y_25/defaults/artist_mix_bg.jpg";
            
            default:
                return baseUrl + "c_fill,w_640,h_640,q_auto/defaults/default_playlist.jpg";
        }
    }
    
    private static String getPrimaryText(PlaylistType type) {
        switch (type) {
            case TRENDING: return "Trending";
            case NEW_DAILY: return "Daily Mix";
            case NEW_RELEASE: return "New Releases";
            case VIRAL: return "Viral Hits";
            case USER_REC:
            case DISCOVERY: return "Discover Weekly";
            case GENRE_MIX: return "Genre Mix";
            case ARTIST_MIX: return "Artist Mix";
            default: return "Playlist";
        }
    }
    
    private static String getSecondaryText(PlaylistType type) {
        switch (type) {
            case TRENDING: return "This Week";
            case NEW_DAILY: return "Updated Daily";
            case NEW_RELEASE: return "Fresh Music";
            case VIRAL: return "Hot Right Now";
            case USER_REC:
            case DISCOVERY: return "Made for You";
            case GENRE_MIX: return "Your Favorites";
            case ARTIST_MIX: return "Your Artists";
            default: return "";
        }
    }
    
    private static String extractPublicIdFromCloudinaryUrl(String url) {
        try {
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String afterUpload = parts[1];
                // Remove version if present
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
            // Return null if extraction fails
        }
        return null;
    }
    
    private static String buildCloudinaryTransformationUrl(String publicId, String primaryText, String secondaryText, PlaylistType type) {
        String baseUrl = "https://res.cloudinary.com/dll5rlqx9/image/upload/";
        
        StringBuilder transformations = new StringBuilder();
        transformations.append("c_fill,w_640,h_640,q_auto,f_auto/");
        
        // Add semi-transparent overlay for better text readability
        transformations.append("l_overlay:black_gradient,o_50/");
        
        // Add primary text
        transformations.append("l_text:Arial_48_bold:")
                      .append(encodeForUrl(primaryText))
                      .append(",co_white,g_south_west,x_20,y_60/");
        
        // Add secondary text if available
        if (secondaryText != null && !secondaryText.isEmpty()) {
            transformations.append("l_text:Arial_24:")
                          .append(encodeForUrl(secondaryText))
                          .append(",co_white,g_south_west,x_20,y_25/");
        }
        
        // Add special badge for Daily Mix
        if (type == PlaylistType.NEW_DAILY) {
            transformations.append("l_overlay:daily_mix_badge,g_north_east,x_20,y_20/");
        }
        
        return baseUrl + transformations.toString() + publicId + ".jpg";
    }
    
    private static String encodeForUrl(String text) {
        return text.replace(" ", "%20")
                  .replace(",", "%2C")
                  .replace("&", "%26");
    }
} 