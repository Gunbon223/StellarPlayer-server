package org.gb.stellarplayer.Request;

import lombok.Data;

/**
 * Request class for Genre operations
 */
@Data
public class GenreRequest {
    private String name;
    private String cover_path;
} 