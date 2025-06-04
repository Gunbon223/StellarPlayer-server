package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PlaylistServiceImplement  implements PlaylistService {
    @Autowired
    PlaylistRepository playlistRepository;
    
    @Autowired
    TrackRepository trackRepository;

    @Override
    public List<Playlist> getPlaylists() {
       List<Playlist> playlists = playlistRepository.findAll();
        playlists.sort(Comparator.comparing(Playlist::getCreatedAt));
        return playlists;
    }

    @Override
    public Playlist getPlaylistById(int id) {
        return playlistRepository.findById(id).orElseThrow(() ->new BadRequestException("Playlist not found"));
    }

    @Override
    public Playlist addPlaylist(Playlist playlist) {
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());
        return playlistRepository.save(playlist);
    }

    @Override
    public Playlist updatePlaylist(Playlist playlist) {
        Playlist existingPlaylist = getPlaylistById(playlist.getId());
        
        if (playlist.getName() != null) {
            existingPlaylist.setName(playlist.getName());
        }
        if (playlist.getCover() != null) {
            existingPlaylist.setCover(playlist.getCover());
        }
        existingPlaylist.setStatus(playlist.isStatus());
        if (playlist.getTracks() != null) {
            existingPlaylist.setTracks(playlist.getTracks());
        }
        existingPlaylist.setUpdatedAt(LocalDateTime.now());
        
        return playlistRepository.save(existingPlaylist);
    }

    @Override
    public Playlist deletePlaylist(int id) {
        Playlist playlist = getPlaylistById(id);
        playlistRepository.deleteById(id);
        return playlist;
    }

    @Override
    public List<Playlist> getAllPlaylists() {
        return List.of();
    }

    @Override
    public List<Track> getTracksByPlaylistId(int playlistId) {
        Playlist playlist = getPlaylistById(playlistId);
        return playlist.getTracks();
    }

    @Override
    public Playlist addTrackToPlaylist(int playlistId, int trackId) {
        Playlist playlist = getPlaylistById(playlistId);
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BadRequestException("Track not found"));

        List<Track> tracks = playlist.getTracks();
        if (tracks.stream().anyMatch(t -> t.getId() == trackId)) {
            throw new BadRequestException("Track already exists in playlist");
        }

        tracks.add(track);
        playlist.setTracks(tracks);
        playlist.setUpdatedAt(LocalDateTime.now());
        
        return playlistRepository.save(playlist);
    }

    @Override
    public Playlist removeTrackFromPlaylist(int playlistId, int trackId) {
        Playlist playlist = getPlaylistById(playlistId);
        List<Track> tracks = playlist.getTracks();
        
        boolean removed = tracks.removeIf(track -> track.getId() == trackId);
        if (!removed) {
            throw new BadRequestException("Track not found in playlist");
        }

        playlist.setTracks(tracks);
        playlist.setUpdatedAt(LocalDateTime.now());
        
        return playlistRepository.save(playlist);
    }
}
