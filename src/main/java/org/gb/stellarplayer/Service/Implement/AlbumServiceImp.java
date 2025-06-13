package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Repository.HistoryRepository;
import org.gb.stellarplayer.Repository.UserFavouriteAlbumRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlbumServiceImp implements AlbumService
{
    @Autowired
    AlbumRepository albumRepository;
    @Autowired
    private TrackService trackService;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private UserFavouriteAlbumRepository userFavouriteAlbumRepository;

    @Override
    public List<Album> getAlbums() {
        return albumRepository.findAll();
    }

    @Override
    public Album getAlbumById(int id) {
        return albumRepository.findById(id).orElseThrow(() -> new BadRequestException("Album not found"));
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
        return albumRepository.findAll();
    }

    @Override
    public Album createAlbum(Album album) {
        return addAlbum(album);
    }

    @Override
    public List<Album> getAlbumsByArtistId(int artistId) {
        return albumRepository.findByArtistsId(artistId);
    }

    @Override
    public List<Album> getAlbumsByReleaseDateRange(LocalDate startDate, LocalDate endDate) {
        return albumRepository.findByReleaseDateBetween(startDate, endDate);
    }

    @Override
    @Transactional
    public void deleteAlbumWithCascade(int albumId) {
        try {
            // Verify album exists
            Album album = getAlbumById(albumId);
            
            System.out.println("Starting cascade delete for album: " + album.getTitle() + " (ID: " + albumId + ")");

            // 1. Get all tracks in this album
            List<Track> albumTracks = trackService.getTrackByAlbumId(albumId);
            System.out.println("Found " + albumTracks.size() + " tracks in this album");

            // 2. Delete history entries for all tracks in this album
            for (Track track : albumTracks) {
                int deletedTrackHistory = historyRepository.deleteByTrackId(track.getId());
                System.out.println("Deleted " + deletedTrackHistory + " history entries for track: " + track.getTitle());
            }

            // 3. Delete history entries for the album itself
            int deletedAlbumHistory = historyRepository.deleteByAlbumId(albumId);
            System.out.println("Deleted " + deletedAlbumHistory + " history entries for album");

            // 4. Delete from user favorites
            int deletedFavorites = userFavouriteAlbumRepository.deleteByAlbumId(albumId);
            System.out.println("Deleted " + deletedFavorites + " favorite entries for album");

            // 5. Remove album reference from all tracks (set album to null)
            for (Track track : albumTracks) {
                track.setAlbum(null);
                trackService.updateTrack(track);
                System.out.println("Removed album reference from track: " + track.getTitle());
            }

            // 6. Finally delete the album itself
            albumRepository.deleteById(albumId);
            System.out.println("Successfully deleted album: " + album.getTitle() + " (ID: " + albumId + ")");

        } catch (Exception e) {
            System.err.println("Error during cascade delete: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete album with cascade: " + e.getMessage(), e);
        }
    }
}
