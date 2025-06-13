package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Genre;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Exception.ResourceNotFoundException;
import org.gb.stellarplayer.Model.Enum.PlaylistType;
import org.gb.stellarplayer.Repository.GenreRepository;
import org.gb.stellarplayer.Repository.PlaylistRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.gb.stellarplayer.Request.GenreRequest;
import org.gb.stellarplayer.Service.GenreService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of GenreService
 */
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;

    @Override
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @Override
    public Genre getGenreById(int id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    @Override
    public Genre createGenre(GenreRequest genreRequest) {
        // Check if genre with the same name already exists
        if (genreRepository.existsByName(genreRequest.getName())) {
            throw new BadRequestException("Genre with name '" + genreRequest.getName() + "' already exists");
        }
        
        // Create new genre
        Genre genre = new Genre();
        genre.setName(genreRequest.getName());
        genre.setCover_path(genreRequest.getCover_path());
        genre.setCreatedAt(LocalDateTime.now());
        genre.setUpdatedAt(LocalDateTime.now());
        
        return genreRepository.save(genre);
    }

    @Override
    public Genre updateGenre(GenreRequest genreRequest, int id) {
        // Find genre by id
        Genre existingGenre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        
        // Check if another genre with the same name exists
        genreRepository.findByName(genreRequest.getName())
                .ifPresent(genre -> {
                    if (genre.getId() != id) {
                        throw new BadRequestException("Genre with name '" + genreRequest.getName() + "' already exists");
                    }
                });
        
        // Update genre
        existingGenre.setName(genreRequest.getName());
        existingGenre.setCover_path(genreRequest.getCover_path());
        existingGenre.setUpdatedAt(LocalDateTime.now());
        
        return genreRepository.save(existingGenre);
    }

    @Override
    public Genre deleteGenre(int id) {
        // Find genre by id
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        
        // Delete genre
        genreRepository.delete(genre);
        
        return genre;
    }
    
    @Override
    public long countTracksByGenre(int genreId) {
        Genre genre = getGenreById(genreId);
        return trackRepository.countByGenre(genre);
    }
    
    @Override
    public List<Track> getTracksByGenre(int genreId) {
        Genre genre = getGenreById(genreId);
        return trackRepository.findByGenresContaining(genre);
    }
    
    @Override
    public Playlist createGenrePlaylist(int genreId, String playlistName, int trackCount) {
        // Validate input
        if (trackCount <= 0 || trackCount > 100) {
            throw new BadRequestException("Track count must be between 1 and 100");
        }
        
        Genre genre = getGenreById(genreId);
        
        // Get random tracks by genre
        List<Track> randomTracks = trackRepository.findRandomTracksByGenreId(genreId, trackCount);
        
        if (randomTracks.isEmpty()) {
            throw new BadRequestException("No tracks found for genre: " + genre.getName());
        }
        
        // Create playlist
        Playlist playlist = new Playlist();
        playlist.setName(playlistName != null && !playlistName.isEmpty() 
                ? playlistName 
                : genre.getName() + " Mix");
        playlist.setType(PlaylistType.PUBLIC);
        playlist.setStatus(true);
        
        // Set default cover as first track's cover if available
        if (!randomTracks.isEmpty() && randomTracks.get(0).getCover() != null) {
            playlist.setCover(randomTracks.get(0).getCover());
        }
        
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());
        playlist.setTracks(randomTracks);
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public Map<String, Object> getGenreStatistics(int genreId) {
        Genre genre = getGenreById(genreId);
        long trackCount = countTracksByGenre(genreId);
        
        List<Track> tracks = getTracksByGenre(genreId);
        
        // Calculate total duration of all tracks in this genre
        int totalDuration = tracks.stream()
                .mapToInt(Track::getDuration)
                .sum();
        
        // Count unique artists in this genre
        long uniqueArtistsCount = tracks.stream()
                .flatMap(track -> track.getArtists().stream())
                .distinct()
                .count();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("genre_id", genreId);
        statistics.put("genre_name", genre.getName());
        statistics.put("genre_cover_path", genre.getCover_path());
        statistics.put("track_count", trackCount);
        statistics.put("total_duration_seconds", totalDuration);
        statistics.put("total_duration_formatted", formatDuration(totalDuration));
        statistics.put("unique_artists_count", uniqueArtistsCount);
        
        return statistics;
    }
    
    /**
     * Format duration in seconds to a human-readable format (HH:MM:SS)
     * @param durationInSeconds Duration in seconds
     * @return Formatted duration string
     */
    private String formatDuration(int durationInSeconds) {
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
} 