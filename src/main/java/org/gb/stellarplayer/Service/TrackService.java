package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Track;

import java.util.List;

public interface TrackService  {
    List<Track> getTracks();
    Track getTrackById(int id);
    List<Track> getTrackByAlbumId(int id);
    Track saveTrack(Track track);
    void deleteTrack(int id);
    Track updateTrack(Track track);

}
