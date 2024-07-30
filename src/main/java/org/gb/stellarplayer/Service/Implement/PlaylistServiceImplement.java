package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class PlaylistServiceImplement  implements PlaylistService {
    @Autowired
    PlaylistRepository playlistRepository;
    @Override
    public List<Playlist> getPlaylists() {
       List<Playlist> playlists = playlistRepository.findAll();
        playlists.sort((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()));
        return playlists;
    }

    @Override
    public Playlist getPlaylistById(int id) {
        return playlistRepository.findById(id).orElseThrow(() ->new BadRequestException("Playlist not found"));
    }

}
