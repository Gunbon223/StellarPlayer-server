package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return albumRepository.findById(id).orElseThrow(() ->new BadRequestException("Album not found"));
    }

    @Override
    public Album addAlbum(Album album) {
        album.setCreatedAt(LocalDateTime.now());
        album.setUpdatedAt(LocalDateTime.now());
        return albumRepository.save(album);
    }

    @Override
    public Album updateAlbum(Album album) {
        Album existingAlbum = getAlbumById(album.getId());
        
        if (album.getTitle() != null) {
            existingAlbum.setTitle(album.getTitle());
        }
        if (album.getCover() != null) {
            existingAlbum.setCover(album.getCover());
        }
        existingAlbum.setStatus(album.isStatus());
        if (album.getArtists() != null) {
            existingAlbum.setArtists(album.getArtists());
        }
        existingAlbum.setUpdatedAt(LocalDateTime.now());
        
        return albumRepository.save(existingAlbum);
    }

    @Override
    public Album deleteAlbum(int id) {
        Album album = getAlbumById(id);
        albumRepository.deleteById(id);
        return album;
    }

    @Override
    public List<Album> getAllAlbums() {
        return List.of();
    }

    @Override
    public Album createAlbum(Album album) {
        return null;
    }

    @Override
    public List<Album> getAlbumsByArtistId(int artistId) {
        return List.of();
    }

    @Override
    public List<Album> getAlbumsByReleaseDateRange(LocalDate startDate, LocalDate endDate) {
        return List.of();
    }
}
