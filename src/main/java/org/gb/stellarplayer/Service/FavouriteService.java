package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.*;

import java.util.List;

public interface FavouriteService {
    List<Favourite> getAllUserFavorites(Integer userId);
    List<Track> getUserFavoriteTracks(Integer userId);
    List<Playlist> getUserFavoritePlaylists(Integer userId);
    List<Album> getUserFavoriteAlbums(Integer userId);
    List<Artist> getUserFavoriteArtists(Integer userId);
    
    // Add to favorites
    boolean addTrackToFavorites(Integer userId, Integer trackId);
    boolean addPlaylistToFavorites(Integer userId, Integer playlistId);
    boolean addAlbumToFavorites(Integer userId, Integer albumId);
    boolean addArtistToFavorites(Integer userId, Integer artistId);
    
    // Remove from favorites
    boolean removeTrackFromFavorites(Integer userId, Integer trackId);
    boolean removePlaylistFromFavorites(Integer userId, Integer playlistId);
    boolean removeAlbumFromFavorites(Integer userId, Integer albumId);
    boolean removeArtistFromFavorites(Integer userId, Integer artistId);
    
    // Check if item is in favorites
    boolean isTrackInFavorites(Integer userId, Integer trackId);
    boolean isPlaylistInFavorites(Integer userId, Integer playlistId);
    boolean isAlbumInFavorites(Integer userId, Integer albumId);
    boolean isArtistInFavorites(Integer userId, Integer artistId);
}
