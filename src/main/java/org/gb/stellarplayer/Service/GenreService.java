package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Genre;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Request.GenreRequest;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Genre management
 */
public interface GenreService {
    
    /**
     * Get all genres
     * @return List of all genres
     */
    List<Genre> getAllGenres();
    
    /**
     * Get genre by ID
     * @param id Genre ID
     * @return Genre with the specified ID
     */
    Genre getGenreById(int id);
    
    /**
     * Create a new genre
     * @param genreRequest Genre request data
     * @return Created genre
     */
    Genre createGenre(GenreRequest genreRequest);
    
    /**
     * Update an existing genre
     * @param genreRequest Updated genre data
     * @param id Genre ID to update
     * @return Updated genre
     */
    Genre updateGenre(GenreRequest genreRequest, int id);
    
    /**
     * Delete a genre
     * @param id Genre ID to delete
     * @return The deleted genre
     */
    Genre deleteGenre(int id);
    
    /**
     * Count tracks by genre
     * @param genreId Genre ID
     * @return Count of tracks with the specified genre
     */
    long countTracksByGenre(int genreId);
    
    /**
     * Get tracks by genre
     * @param genreId Genre ID
     * @return List of tracks with the specified genre
     */
    List<Track> getTracksByGenre(int genreId);
    
    /**
     * Create a playlist with random tracks from a specific genre
     * @param genreId Genre ID
     * @param playlistName Name of the playlist to create
     * @param trackCount Number of random tracks to include
     * @return Created playlist
     */
    Playlist createGenrePlaylist(int genreId, String playlistName, int trackCount);
    
    /**
     * Get genre statistics including track count
     * @param genreId Genre ID
     * @return Map containing statistics
     */
    Map<String, Object> getGenreStatistics(int genreId);
} 