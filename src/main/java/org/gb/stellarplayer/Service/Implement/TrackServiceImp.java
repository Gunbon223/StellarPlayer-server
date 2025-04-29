package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        // Set creation and update timestamps
        track.setCreatedAt(LocalDateTime.now());
        track.setUpdatedAt(LocalDateTime.now());
        
        // If album ID is set, make sure it exists
        if (track.getAlbum() != null && track.getAlbum().getId() != null) {
            Album album = albumRepository.findById(track.getAlbum().getId())
                .orElseThrow(() -> new BadRequestException("Album not found with id: " + track.getAlbum().getId()));
            track.setAlbum(album);
        }
        
        return trackRepository.save(track);
    }

    @Override
    public void deleteTrack(int id) {
        Track track = getTrackById(id); // Verify track exists
        trackRepository.deleteById(id);
    }

    @Override
    public Track updateTrack(Track track) {
        // Verify track exists
        Track existingTrack = getTrackById(track.getId());
        
        // Update all fields
        if (track.getTitle() != null) {
            existingTrack.setTitle(track.getTitle());
        }
        
        if (track.getDuration() > 0) {
            existingTrack.setDuration(track.getDuration());
        }
        
        if (track.getPath() != null) {
            existingTrack.setPath(track.getPath());
        }
        
        if (track.getCover() != null) {
            existingTrack.setCover(track.getCover());
        }
        
        if (track.getLyrics() != null) {
            existingTrack.setLyrics(track.getLyrics());
        }
        
        if (track.getAlbum() != null && track.getAlbum().getId() != null) {
            Album album = albumRepository.findById(track.getAlbum().getId())
                .orElseThrow(() -> new BadRequestException("Album not found with id: " + track.getAlbum().getId()));
            existingTrack.setAlbum(album);
        }
        
        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
            existingTrack.setArtists(track.getArtists());
        }
        
        existingTrack.setStatus(track.isStatus());
        existingTrack.setUpdatedAt(LocalDateTime.now());
        
        return trackRepository.save(existingTrack);
    }
}
