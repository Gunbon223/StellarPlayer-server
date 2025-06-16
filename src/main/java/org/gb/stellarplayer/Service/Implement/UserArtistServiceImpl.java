package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.DTO.UserArtistDTO;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserArtistRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Repository.ArtistRepository;
import org.gb.stellarplayer.Service.UserArtistService;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Service.AlbumService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserArtistServiceImpl implements UserArtistService {
    
    private final UserArtistRepository userArtistRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final TrackService trackService;
    private final AlbumService albumService;

    @Override
    @Transactional
    public UserArtist linkUserToArtist(Integer userId, Integer artistId) {
        // Verify user and artist exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new BadRequestException("Artist not found"));
        
        // Check if relationship already exists
        if (userArtistRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new BadRequestException("User is already linked to this artist");
        }
        
        UserArtist userArtist = UserArtist.builder()
                .user(user)
                .artist(artist)
                .build();
        
        return userArtistRepository.save(userArtist);
    }

    @Override
    @Transactional
    public void unlinkUserFromArtist(Integer userId, Integer artistId) {
        if (!userArtistRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new BadRequestException("User is not linked to this artist");
        }
        userArtistRepository.deleteByUserIdAndArtistId(userId, artistId);
    }

    @Override
    public List<Artist> getUserArtists(Integer userId) {
        return userArtistRepository.findArtistsByUserId(userId);
    }

    @Override
    public boolean canUserManageArtist(Integer userId, Integer artistId) {
        return userArtistRepository.existsByUserIdAndArtistId(userId, artistId);
    }

    @Override
    public boolean canUserManageTrack(Integer userId, Integer trackId) {
        try {
            Track track = trackService.getTrackById(trackId);
            
            if (track.getArtists() != null && !track.getArtists().isEmpty()) {
                // Check if user can manage any of the track's artists
                for (Artist artist : track.getArtists()) {
                    if (canUserManageArtist(userId, artist.getId())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canUserManageAlbum(Integer userId, Integer albumId) {
        try {
            Album album = albumService.getAlbumById(albumId);
            
            if (album.getArtists() != null && !album.getArtists().isEmpty()) {
                // Check if user can manage any of the album's artists
                for (Artist artist : album.getArtists()) {
                    if (canUserManageArtist(userId, artist.getId())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<UserArtist> getUserArtistRelations(Integer userId) {
        if (userId == null) {
            return userArtistRepository.findAll();
        }
        return userArtistRepository.findByUserId(userId);
    }

    @Override
    public List<UserArtistDTO> getAllUserArtistRelationships() {
        List<UserArtist> relationships = userArtistRepository.findAllWithUserAndArtist();
        return relationships.stream()
                .map(UserArtistDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserArtistDTO> getUserArtistRelationshipsByUserId(Integer userId) {
        List<UserArtist> relationships = userArtistRepository.findByUserIdWithUserAndArtist(userId);
        return relationships.stream()
                .map(UserArtistDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserArtistDTO> getUserArtistRelationshipsByArtistId(Integer artistId) {
        List<UserArtist> relationships = userArtistRepository.findByArtistIdWithUserAndArtist(artistId);
        return relationships.stream()
                .map(UserArtistDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 