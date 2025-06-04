package org.gb.stellarplayer.Request;

import lombok.Data;

import java.util.List;

@Data
public class TrackRequest {
    private String title;
    private List<Integer> artist_id;
    private String genre;
    private int duration;

    private String path;
    private String cover;
    private String lyrics;
    private boolean status;
    private long playCount;
    private int albumId;
}
