package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Comparator;
import java.util.List;

@Service
public class PlaylistServiceImplement  implements PlaylistService {
    @Autowired
    PlaylistRepository playlistRepository;
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
        return null;
    }

    @Override
    public Playlist updatePlaylist(Playlist playlist) {
        return null;
    }

    @Override
    public Playlist deletePlaylist(int id) {
        return null;
    }

}
