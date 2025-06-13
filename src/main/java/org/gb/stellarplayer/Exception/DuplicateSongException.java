package org.gb.stellarplayer.Exception;

public class DuplicateSongException extends RuntimeException {
    
    private final String songTitle;
    private final String artistNames;
    private final Integer existingTrackId;
    
    public DuplicateSongException(String songTitle, String artistNames, Integer existingTrackId) {
        super(String.format("Song already exists: '%s' by %s (ID: %d)", songTitle, artistNames, existingTrackId));
        this.songTitle = songTitle;
        this.artistNames = artistNames;
        this.existingTrackId = existingTrackId;
    }
    
    public String getSongTitle() {
        return songTitle;
    }
    
    public String getArtistNames() {
        return artistNames;
    }
    
    public Integer getExistingTrackId() {
        return existingTrackId;
    }
} 