package org.gb.stellarplayer.Service.Implement;

import org.gb.stellarplayer.DTO.TrackAdminDTO;
import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.AlbumRepository;
import org.gb.stellarplayer.Repository.HistoryRepository;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Repository.UserFavouriteTrackRepository;
import org.gb.stellarplayer.Repository.UserTrackInteractionRepository;
import org.gb.stellarplayer.Service.AlbumService;
import org.gb.stellarplayer.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrackServiceImp implements TrackService {
    @Autowired
    TrackRepository trackRepository;
    @Autowired
    AlbumRepository albumRepository;
    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    UserFavouriteTrackRepository userFavouriteTrackRepository;
    @Autowired
    UserTrackInteractionRepository userTrackInteractionRepository;

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
        
        // Update only non-null fields
        if (track.getTitle() != null) {
            existingTrack.setTitle(track.getTitle());
        }
        if (track.getPath() != null) {
            existingTrack.setPath(track.getPath());
        }
        if (track.getDuration() != 0) {
            existingTrack.setDuration(track.getDuration());
        }
        if (track.getCover() != null) {
            existingTrack.setCover(track.getCover());
        }
        if (track.getLyrics() != null) {
            existingTrack.setLyrics(track.getLyrics());
        }
        
        // Update the status and play count
        existingTrack.setStatus(track.isStatus());
        if (track.getPlayCount() != null) {
            existingTrack.setPlayCount(track.getPlayCount());
        }
        
        // Update album if specified
        if (track.getAlbum() != null && track.getAlbum().getId() != null) {
            Album album = albumRepository.findById(track.getAlbum().getId())
                .orElseThrow(() -> new BadRequestException("Album not found with id: " + track.getAlbum().getId()));
            existingTrack.setAlbum(album);
        }
        
        // Update artists if specified
        if (track.getArtists() != null) {
            existingTrack.setArtists(track.getArtists());
        }
        
        // Update genres if specified
        if (track.getGenres() != null) {
            existingTrack.setGenres(track.getGenres());
        }
        
        // Update timestamp
        existingTrack.setUpdatedAt(LocalDateTime.now());
        
        return trackRepository.save(existingTrack);
    }
    
    @Override
    public Map<String, Object> getPaginatedTracks(int page, int pageSize, String sortBy, boolean ascending) {
        // Validate sorting field
        String sortField = "id"; // Default sort field
        
        // Map the sortBy parameter to the actual field name
        if ("title".equalsIgnoreCase(sortBy)) {
            sortField = "title";
        } else if ("artistName".equalsIgnoreCase(sortBy)) {
            // For artist name, we will need to handle this in memory since it's a relationship
            sortField = "id"; // We'll sort by ID and handle artist sorting later
        }
        
        // Create pageable object with sorting
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortField));
        
        // Get paginated tracks
        Page<Track> trackPage = trackRepository.findAll(pageable);
        
        // Convert to DTOs
        List<TrackAdminDTO> tracks = trackPage.getContent().stream()
            .map(TrackAdminDTO::fromEntity)
            .collect(Collectors.toList());
        

        
        // Create response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("tracks", tracks);
        response.put("currentPage", trackPage.getNumber());
        response.put("totalItems", trackPage.getTotalElements());
        response.put("totalPages", trackPage.getTotalPages());
        
        return response;
    }
    
    @Override
    public List<Track> getTracksByArtistId(int artistId) {
        return trackRepository.findByArtistsIdOrderByCreatedAtDesc(artistId, Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Track> getUnapprovedTracks(Pageable pageable) {
        return trackRepository.findByStatusFalse(pageable);
    }
    
    /**
     * Helper method to compare track DTOs by artist name
     */

    @Transactional
    @Override
    public void deleteTrackWithCascade(int id) {
        try {
            // Verify track exists
            Track track = getTrackById(id);
            
            System.out.println("Starting cascade delete for track: " + track.getTitle() + " (ID: " + id + ")");

            // 1. Delete from history table
            int deletedHistory = historyRepository.deleteByTrackId(id);
            System.out.println("Deleted " + deletedHistory + " history entries for track");

            // 2. Delete from user favorites
            int deletedFavorites = userFavouriteTrackRepository.deleteByTrackId(id);
            System.out.println("Deleted " + deletedFavorites + " favorite entries for track");

            // 3. Delete from user track interactions
            userTrackInteractionRepository.deleteByTrackId(id);
            System.out.println("Deleted user track interactions for track");

            // 4. Remove track from all playlists
            List<Playlist> allPlaylists = playlistRepository.findAll();
            for (Playlist playlist : allPlaylists) {
                if (playlist.getTracks() != null) {
                    boolean wasRemoved = playlist.getTracks().removeIf(t -> t.getId().equals(id));
                    if (wasRemoved) {
                        playlist.setUpdatedAt(LocalDateTime.now());
                        playlistRepository.save(playlist);
                        System.out.println("Removed track from playlist: " + playlist.getName());
                    }
                }
            }

            // 5. Finally delete the track itself
            trackRepository.deleteById(id);
            
            System.out.println("Successfully deleted track: " + track.getTitle() + " (ID: " + id + ")");
            
        } catch (Exception e) {
            System.err.println("Error during cascade delete: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete track with cascade: " + e.getMessage(), e);
        }
    }
}
