package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.DTO.FavouriteTrackDTO;
import org.gb.stellarplayer.DTO.FavouriteAlbumDTO;
import org.gb.stellarplayer.DTO.FavouriteArtistDTO;
import org.gb.stellarplayer.DTO.FavouritePlaylistDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface FavouriteService {
    // Track favorites
    UserFavouriteTrack addTrackToFavourites(Integer userId, Integer trackId);
    void removeTrackFromFavourites(Integer userId, Integer trackId);
    List<UserFavouriteTrack> getUserFavouriteTracks(Integer userId);
    List<FavouriteTrackDTO> getUserFavouriteTrackEntities(Integer userId);
    boolean isTrackFavourite(Integer userId, Integer trackId);
    
    // Paginated track favorites
    Map<String, Object> getUserFavouriteTracksPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending);
    
    // Album favorites
    UserFavouriteAlbum addAlbumToFavourites(Integer userId, Integer albumId);
    void removeAlbumFromFavourites(Integer userId, Integer albumId);
    List<UserFavouriteAlbum> getUserFavouriteAlbums(Integer userId);
    List<FavouriteAlbumDTO> getUserFavouriteAlbumEntities(Integer userId);
    boolean isAlbumFavourite(Integer userId, Integer albumId);
    
    // Paginated album favorites
    Map<String, Object> getUserFavouriteAlbumsPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending);
    
    // Artist favorites
    UserFavouriteArtist addArtistToFavourites(Integer userId, Integer artistId);
    void removeArtistFromFavourites(Integer userId, Integer artistId);
    List<UserFavouriteArtist> getUserFavouriteArtists(Integer userId);
    List<FavouriteArtistDTO> getUserFavouriteArtistEntities(Integer userId);
    boolean isArtistFavourite(Integer userId, Integer artistId);
    
    // Paginated artist favorites
    Map<String, Object> getUserFavouriteArtistsPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending);
    
    // Playlist favorites
    UserFavouritePlaylist addPlaylistToFavourites(Integer userId, Integer playlistId);
    void removePlaylistFromFavourites(Integer userId, Integer playlistId);
    List<UserFavouritePlaylist> getUserFavouritePlaylists(Integer userId);
    List<FavouritePlaylistDTO> getUserFavouritePlaylistEntities(Integer userId);
    boolean isPlaylistFavourite(Integer userId, Integer playlistId);
    
    // Paginated playlist favorites
    Map<String, Object> getUserFavouritePlaylistsPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending);
    
    // Statistics
    long getTrackFavouriteCount(Integer trackId);
    long getAlbumFavouriteCount(Integer albumId);
    long getArtistFavouriteCount(Integer artistId);
    long getPlaylistFavouriteCount(Integer playlistId);
}
