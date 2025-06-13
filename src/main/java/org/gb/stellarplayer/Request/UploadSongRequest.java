package org.gb.stellarplayer.Request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UploadSongRequest {
    private String title;
    private Integer releaseYear;
    private List<String> artistNames;
    private String albumTitle;
    private List<String> genreNames;
} 