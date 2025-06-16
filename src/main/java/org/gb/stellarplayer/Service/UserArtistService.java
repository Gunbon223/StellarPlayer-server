package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.DTO.UserArtistDTO;
import org.gb.stellarplayer.Entites.UserArtist;
import org.gb.stellarplayer.Entites.Artist;
import java.util.List;

public interface UserArtistService {
    UserArtist linkUserToArtist(Integer userId, Integer artistId);
    void unlinkUserFromArtist(Integer userId, Integer artistId);
    List<Artist> getUserArtists(Integer userId);
    boolean canUserManageArtist(Integer userId, Integer artistId);
    boolean canUserManageTrack(Integer userId, Integer trackId);
    boolean canUserManageAlbum(Integer userId, Integer albumId);
    List<UserArtist> getUserArtistRelations(Integer userId);
    
    // New methods for DTOs
    List<UserArtistDTO> getAllUserArtistRelationships();
    List<UserArtistDTO> getUserArtistRelationshipsByUserId(Integer userId);
    List<UserArtistDTO> getUserArtistRelationshipsByArtistId(Integer artistId);
} 