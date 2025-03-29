package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TrackServiceImp implements TrackService {
    @Autowired
    TrackRepository trackRepository;
    @Autowired
    AlbumRepository albumRepository;

    @Override
    public List<Track> getTracks() {
        return trackRepository.findAll();
    }

    @Override
    public Track getTrackById(int id) {
        return trackRepository.findById(id).orElseThrow(()-> new BadRequestException("Track not found"));
    }

    @Override
    public List<Track> getTrackByAlbumId(int id) {
        return trackRepository.findByAlbumId(id);
    }

    @Override
    public Track saveTrack(Track track) {
        return trackRepository.save(track);
    }

    @Override
    public void deleteTrack(int id) {

    }

    @Override
    public Track updateTrack(Track track) {
        return null;
    }


}
