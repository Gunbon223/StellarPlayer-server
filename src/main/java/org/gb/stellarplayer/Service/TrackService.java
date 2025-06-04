package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Track;

import java.util.List;
import java.util.Map;

public interface TrackService  {
    List<Track> getTracks();
    Track getTrackById(int id);
    List<Track> getTrackByAlbumId(int id);
    Track saveTrack(Track track);
    void deleteTrack(int id);
    Track updateTrack(Track track);
    
    /**
     * Get paginated and sorted tracks
     * @param page Page number (zero-based)
     * @param pageSize Number of items per page
     * @param sortBy Field to sort by (id, title, artistName)
     * @param ascending Sort direction (true for ascending, false for descending)
     * @return Map containing paginated results and metadata
     */
    Map<String, Object> getPaginatedTracks(int page, int pageSize, String sortBy, boolean ascending);
}
