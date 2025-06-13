package org.gb.stellarplayer.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Model.Enum.PlaylistType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaylistCoverGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlaylistCoverGeneratorService.class);
    private final Cloudinary cloudinary;
    
    private static final int COVER_SIZE = 640;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final String FONT_NAME = "Arial";
    
    public String generatePlaylistCover(Playlist playlist) {
        try {
            String baseImageUrl = getBaseImageUrl(playlist);
            if (baseImageUrl == null) {
                return getDefaultCover(playlist.getType());
            }
            
            BufferedImage baseImage = downloadImage(baseImageUrl);
            BufferedImage processedImage = processImage(baseImage, playlist);
            
            return uploadGeneratedCover(processedImage, playlist);
            
        } catch (Exception e) {
            logger.error("Failed to generate playlist cover for: {}", playlist.getName(), e);
            return getDefaultCover(playlist.getType());
        }
    }
    
    private String getBaseImageUrl(Playlist playlist) {
        if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {
            Track firstTrack = playlist.getTracks().get(0);
            if (firstTrack.getCover() != null && !firstTrack.getCover().isEmpty()) {
                return firstTrack.getCover();
            }
            if (firstTrack.getAlbum() != null && firstTrack.getAlbum().getCover() != null) {
                return firstTrack.getAlbum().getCover();
            }
        }
        return null;
    }
    
    private BufferedImage downloadImage(String imageUrl) throws IOException {
        try {
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();
            BufferedImage image = ImageIO.read(inputStream);
            inputStream.close();
            return image;
        } catch (Exception e) {
            logger.warn("Failed to download image from: {}", imageUrl);
            throw new IOException("Cannot download image: " + e.getMessage());
        }
    }
    
    private BufferedImage processImage(BufferedImage baseImage, Playlist playlist) {
        BufferedImage squareImage = createSquareImage(baseImage);
        
        Graphics2D g2d = squareImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        addGradientOverlay(g2d, squareImage.getWidth(), squareImage.getHeight());
        addTextOverlay(g2d, playlist, squareImage.getWidth(), squareImage.getHeight());
        
        g2d.dispose();
        return squareImage;
    }
    
    private BufferedImage createSquareImage(BufferedImage original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        int cropSize = Math.min(originalWidth, originalHeight);
        int x = (originalWidth - cropSize) / 2;
        int y = (originalHeight - cropSize) / 2;
        
        BufferedImage cropped = original.getSubimage(x, y, cropSize, cropSize);
        
        BufferedImage resized = new BufferedImage(COVER_SIZE, COVER_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(cropped, 0, 0, COVER_SIZE, COVER_SIZE, null);
        g2d.dispose();
        
        return resized;
    }
    
    private void addGradientOverlay(Graphics2D g2d, int width, int height) {
        GradientPaint gradient = new GradientPaint(
            0, height * 0.6f, new Color(0, 0, 0, 0),
            0, height, new Color(0, 0, 0, 150)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, (int)(height * 0.6), width, (int)(height * 0.4));
    }
    
    private void addTextOverlay(Graphics2D g2d, Playlist playlist, int width, int height) {
        String primaryText = getPrimaryText(playlist);
        String secondaryText = getSecondaryText(playlist);
        
        g2d.setColor(TEXT_COLOR);
        
        Font primaryFont = new Font(FONT_NAME, Font.BOLD, 48);
        g2d.setFont(primaryFont);
        
        int primaryX = 20;
        int primaryY = height - 60;
        g2d.drawString(primaryText, primaryX, primaryY);
        
        if (secondaryText != null && !secondaryText.isEmpty()) {
            Font secondaryFont = new Font(FONT_NAME, Font.PLAIN, 24);
            g2d.setFont(secondaryFont);
            
            int secondaryX = 20;
            int secondaryY = height - 25;
            g2d.drawString(secondaryText, secondaryX, secondaryY);
        }
        
        if (isRecommendationPlaylist(playlist.getType())) {
            addPlaylistTypeBadge(g2d, playlist.getType(), width, height);
        }
    }
    
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
                return null;
        }
    }
    
    private void addPlaylistTypeBadge(Graphics2D g2d, PlaylistType type, int width, int height) {
        if (type == PlaylistType.NEW_DAILY) {
            int badgeSize = 40;
            int badgeX = width - badgeSize - 20;
            int badgeY = 20;
            
            g2d.setColor(new Color(29, 185, 84)); // Spotify green
            g2d.fillOval(badgeX, badgeY, badgeSize, badgeSize);
            
            g2d.setColor(Color.WHITE);
            Font badgeFont = new Font(FONT_NAME, Font.BOLD, 18);
            g2d.setFont(badgeFont);
            FontMetrics metrics = g2d.getFontMetrics();
            
            String number = "01";
            int textX = badgeX + (badgeSize - metrics.stringWidth(number)) / 2;
            int textY = badgeY + ((badgeSize - metrics.getHeight()) / 2) + metrics.getAscent();
            g2d.drawString(number, textX, textY);
        }
    }
    
    private boolean isRecommendationPlaylist(PlaylistType type) {
        return type != PlaylistType.PUBLIC && type != PlaylistType.PRIVATE;
    }
    
    private String uploadGeneratedCover(BufferedImage image, Playlist playlist) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            
            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes,
                ObjectUtils.asMap(
                    "folder", "playlist_covers",
                    "resource_type", "image",
                    "public_id", "playlist_" + playlist.getId() + "_" + System.currentTimeMillis(),
                    "quality", "auto",
                    "format", "jpg"
                ));
            
            return (String) uploadResult.get("secure_url");
            
        } catch (Exception e) {
            logger.error("Failed to upload generated cover for playlist: {}", playlist.getName(), e);
            throw new IOException("Upload failed: " + e.getMessage());
        }
    }
    
    private String getDefaultCover(PlaylistType type) {
        switch (type) {
            case TRENDING:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/trending_cover.jpg";
            case NEW_DAILY:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/daily_mix_cover.jpg";
            case NEW_RELEASE:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/new_releases_cover.jpg";
            case VIRAL:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/viral_cover.jpg";
            case USER_REC:
            case DISCOVERY:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/discover_weekly_cover.jpg";
            case GENRE_MIX:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/genre_mix_cover.jpg";
            case ARTIST_MIX:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/artist_mix_cover.jpg";
            default:
                return "https://res.cloudinary.com/dll5rlqx9/image/upload/v1/defaults/default_playlist_cover.jpg";
        }
    }
} 