package org.gb.stellarplayer.Response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongUploadResponse {
    private boolean success;
    private String message;
    private TrackInfo track;
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrackInfo {
        private Integer id;
        private String title;
        private Integer duration;
        private String path;
        private String cover;
        private Integer releaseYear;
        private AlbumInfo album;
        private List<ArtistInfo> artists;
        private List<GenreInfo> genres;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ArtistInfo {
        private Integer id;
        private String name;
        private String avatar;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlbumInfo {
        private Integer id;
        private String title;
        private String cover;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenreInfo {
        private Integer id;
        private String name;
    }
} 