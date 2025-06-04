package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.DTO.FavouriteTrackDTO;
import org.gb.stellarplayer.DTO.FavouriteAlbumDTO;
import org.gb.stellarplayer.DTO.FavouriteArtistDTO;
import org.gb.stellarplayer.DTO.FavouritePlaylistDTO;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Exception.ResourceNotFoundException;
import org.gb.stellarplayer.Repository.*;
import org.gb.stellarplayer.Service.FavouriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavouriteServiceImplement implements FavouriteService {
    
    private final UserFavouriteTrackRepository trackFavouriteRepository;
    private final UserFavouriteAlbumRepository albumFavouriteRepository;
    private final UserFavouriteArtistRepository artistFavouriteRepository;
    private final UserFavouritePlaylistRepository playlistFavouriteRepository;
    
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final PlaylistRepository playlistRepository;

    // Track favorites
    @Override
    public UserFavouriteTrack addTrackToFavourites(Integer userId, Integer trackId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validate track exists
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found"));
        
        // Check if already in favourites
        if (trackFavouriteRepository.existsByUserIdAndTrackId(userId, trackId)) {
            throw new BadRequestException("Track is already in favourites");
        }
        
        UserFavouriteTrack favourite = UserFavouriteTrack.builder()
                .user(user)
                .track(track)
                .build();
        
        return trackFavouriteRepository.save(favourite);
    }

    @Override
    public void removeTrackFromFavourites(Integer userId, Integer trackId) {
        if (!trackFavouriteRepository.existsByUserIdAndTrackId(userId, trackId)) {
            throw new ResourceNotFoundException("Track not found in favourites");
        }
        trackFavouriteRepository.deleteByUserIdAndTrackId(userId, trackId);
    }

    @Override
    public List<UserFavouriteTrack> getUserFavouriteTracks(Integer userId) {
        return trackFavouriteRepository.findByUserIdWithTrackOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<FavouriteTrackDTO> getUserFavouriteTrackEntities(Integer userId) {
        return trackFavouriteRepository.findByUserIdWithTrackOrderByCreatedAtDesc(userId)
                .stream()
                .map(FavouriteTrackDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTrackFavourite(Integer userId, Integer trackId) {
        return trackFavouriteRepository.existsByUserIdAndTrackId(userId, trackId);
    }

    @Override
    public Map<String, Object> getUserFavouriteTracksPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending) {
        Sort sort = createSort(sortBy, ascending);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        
        Page<UserFavouriteTrack> favouritePage = trackFavouriteRepository.findByUserIdWithTrack(userId, pageable);
        
        List<FavouriteTrackDTO> tracks = favouritePage.getContent()
                .stream()
                .map(FavouriteTrackDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("tracks", tracks);
        response.put("currentPage", favouritePage.getNumber());
        response.put("totalItems", favouritePage.getTotalElements());
        response.put("totalPages", favouritePage.getTotalPages());
        response.put("pageSize", favouritePage.getSize());
        response.put("hasNext", favouritePage.hasNext());
        response.put("hasPrevious", favouritePage.hasPrevious());
        response.put("isFirst", favouritePage.isFirst());
        response.put("isLast", favouritePage.isLast());
        
        return response;
    }

    // Album favorites
    @Override
    public UserFavouriteAlbum addAlbumToFavourites(Integer userId, Integer albumId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));
        
        if (albumFavouriteRepository.existsByUserIdAndAlbumId(userId, albumId)) {
            throw new BadRequestException("Album is already in favourites");
        }
        
        UserFavouriteAlbum favourite = UserFavouriteAlbum.builder()
                .user(user)
                .album(album)
                .build();
        
        return albumFavouriteRepository.save(favourite);
    }

    @Override
    public void removeAlbumFromFavourites(Integer userId, Integer albumId) {
        if (!albumFavouriteRepository.existsByUserIdAndAlbumId(userId, albumId)) {
            throw new ResourceNotFoundException("Album not found in favourites");
        }
        albumFavouriteRepository.deleteByUserIdAndAlbumId(userId, albumId);
    }

    @Override
    public List<UserFavouriteAlbum> getUserFavouriteAlbums(Integer userId) {
        return albumFavouriteRepository.findByUserIdWithAlbumOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<FavouriteAlbumDTO> getUserFavouriteAlbumEntities(Integer userId) {
        return albumFavouriteRepository.findByUserIdWithAlbumOrderByCreatedAtDesc(userId)
                .stream()
                .map(FavouriteAlbumDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAlbumFavourite(Integer userId, Integer albumId) {
        return albumFavouriteRepository.existsByUserIdAndAlbumId(userId, albumId);
    }

    @Override
    public Map<String, Object> getUserFavouriteAlbumsPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending) {
        Sort sort = createSort(sortBy, ascending);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        
        Page<UserFavouriteAlbum> favouritePage = albumFavouriteRepository.findByUserIdWithAlbum(userId, pageable);
        
        List<FavouriteAlbumDTO> albums = favouritePage.getContent()
                .stream()
                .map(FavouriteAlbumDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("albums", albums);
        response.put("currentPage", favouritePage.getNumber());
        response.put("totalItems", favouritePage.getTotalElements());
        response.put("totalPages", favouritePage.getTotalPages());
        response.put("pageSize", favouritePage.getSize());
        response.put("hasNext", favouritePage.hasNext());
        response.put("hasPrevious", favouritePage.hasPrevious());
        response.put("isFirst", favouritePage.isFirst());
        response.put("isLast", favouritePage.isLast());
        
        return response;
    }

    // Artist favorites
    @Override
    public UserFavouriteArtist addArtistToFavourites(Integer userId, Integer artistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));
        
        if (artistFavouriteRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new BadRequestException("Artist is already in favourites");
        }
        
        UserFavouriteArtist favourite = UserFavouriteArtist.builder()
                .user(user)
                .artist(artist)
                .build();
        
        return artistFavouriteRepository.save(favourite);
    }

    @Override
    public void removeArtistFromFavourites(Integer userId, Integer artistId) {
        if (!artistFavouriteRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new ResourceNotFoundException("Artist not found in favourites");
        }
        artistFavouriteRepository.deleteByUserIdAndArtistId(userId, artistId);
    }

    @Override
    public List<UserFavouriteArtist> getUserFavouriteArtists(Integer userId) {
        return artistFavouriteRepository.findByUserIdWithArtistOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<FavouriteArtistDTO> getUserFavouriteArtistEntities(Integer userId) {
        return artistFavouriteRepository.findByUserIdWithArtistOrderByCreatedAtDesc(userId)
                .stream()
                .map(FavouriteArtistDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isArtistFavourite(Integer userId, Integer artistId) {
        return artistFavouriteRepository.existsByUserIdAndArtistId(userId, artistId);
    }

    @Override
    public Map<String, Object> getUserFavouriteArtistsPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending) {
        Sort sort = createSort(sortBy, ascending);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        
        Page<UserFavouriteArtist> favouritePage = artistFavouriteRepository.findByUserIdWithArtist(userId, pageable);
        
        List<FavouriteArtistDTO> artists = favouritePage.getContent()
                .stream()
                .map(FavouriteArtistDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("artists", artists);
        response.put("currentPage", favouritePage.getNumber());
        response.put("totalItems", favouritePage.getTotalElements());
        response.put("totalPages", favouritePage.getTotalPages());
        response.put("pageSize", favouritePage.getSize());
        response.put("hasNext", favouritePage.hasNext());
        response.put("hasPrevious", favouritePage.hasPrevious());
        response.put("isFirst", favouritePage.isFirst());
        response.put("isLast", favouritePage.isLast());
        
        return response;
    }

    // Playlist favorites
    @Override
    public UserFavouritePlaylist addPlaylistToFavourites(Integer userId, Integer playlistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));
        
        if (playlistFavouriteRepository.existsByUserIdAndPlaylistId(userId, playlistId)) {
            throw new BadRequestException("Playlist is already in favourites");
        }
        
        UserFavouritePlaylist favourite = UserFavouritePlaylist.builder()
                .user(user)
                .playlist(playlist)
                .build();
        
        return playlistFavouriteRepository.save(favourite);
    }

    @Override
    public void removePlaylistFromFavourites(Integer userId, Integer playlistId) {
        if (!playlistFavouriteRepository.existsByUserIdAndPlaylistId(userId, playlistId)) {
            throw new ResourceNotFoundException("Playlist not found in favourites");
        }
        playlistFavouriteRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
    }

    @Override
    public List<UserFavouritePlaylist> getUserFavouritePlaylists(Integer userId) {
        return playlistFavouriteRepository.findByUserIdWithPlaylistOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<FavouritePlaylistDTO> getUserFavouritePlaylistEntities(Integer userId) {
        return playlistFavouriteRepository.findByUserIdWithPlaylistOrderByCreatedAtDesc(userId)
                .stream()
                .map(FavouritePlaylistDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPlaylistFavourite(Integer userId, Integer playlistId) {
        return playlistFavouriteRepository.existsByUserIdAndPlaylistId(userId, playlistId);
    }

    @Override
    public Map<String, Object> getUserFavouritePlaylistsPaginated(Integer userId, int page, int pageSize, String sortBy, boolean ascending) {
        Sort sort = createSort(sortBy, ascending);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        
        Page<UserFavouritePlaylist> favouritePage = playlistFavouriteRepository.findByUserIdWithPlaylist(userId, pageable);
        
        List<FavouritePlaylistDTO> playlists = favouritePage.getContent()
                .stream()
                .map(FavouritePlaylistDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("playlists", playlists);
        response.put("currentPage", favouritePage.getNumber());
        response.put("totalItems", favouritePage.getTotalElements());
        response.put("totalPages", favouritePage.getTotalPages());
        response.put("pageSize", favouritePage.getSize());
        response.put("hasNext", favouritePage.hasNext());
        response.put("hasPrevious", favouritePage.hasPrevious());
        response.put("isFirst", favouritePage.isFirst());
        response.put("isLast", favouritePage.isLast());
        
        return response;
    }

    // Statistics
    @Override
    public long getTrackFavouriteCount(Integer trackId) {
        return trackFavouriteRepository.countByTrackId(trackId);
    }

    @Override
    public long getAlbumFavouriteCount(Integer albumId) {
        return albumFavouriteRepository.countByAlbumId(albumId);
    }

    @Override
    public long getArtistFavouriteCount(Integer artistId) {
        return artistFavouriteRepository.countByArtistId(artistId);
    }

    @Override
    public long getPlaylistFavouriteCount(Integer playlistId) {
        return playlistFavouriteRepository.countByPlaylistId(playlistId);
    }

    // Helper method to create Sort object
    private Sort createSort(String sortBy, boolean ascending) {
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Map sortBy parameter to actual field names
        switch (sortBy.toLowerCase()) {
            case "id":
                return Sort.by(direction, "id");
            case "title":
                // For tracks, albums, artists, playlists - sort by the entity's title/name
                return Sort.by(direction, "track.title", "album.title", "artist.name", "playlist.name");
            case "created":
            case "createddate":
            case "createdat":
                return Sort.by(direction, "createdAt");
            case "updated":
            case "updateddate":
            case "updatedat":
                return Sort.by(direction, "updatedAt");
            default:
                // Default sort by creation date descending
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
} 