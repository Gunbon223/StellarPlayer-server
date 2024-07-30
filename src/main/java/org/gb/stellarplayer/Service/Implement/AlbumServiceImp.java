package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
@Service
public class AlbumServiceImp implements AlbumService
{
    @Autowired
    AlbumRepository albumRepository;
    @Override
    public List<Album> getAlbums() {
        return albumRepository.findAll();
    }

    @Override
    public Album getAlbumById(int id) {
        return albumRepository.findById(id).orElseThrow(() ->new BadRequestException("Playlist not found"));
    }
}
