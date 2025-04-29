package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.Repository.*;
import org.gb.stellarplayer.Service.FavouriteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavouriteServiceImp implements FavouriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    @Override
    public List<Favourite> getAllUserFavorites(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return favoriteRepository.findByUser(user);
    }

    @Override
    public List<Track> getUserFavoriteTracks(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return favoriteRepository.findByUserAndTrackIsNotNull(user)
                .stream()
                .map(Favourite::getTrack)
                .collect(Collectors.toList());
    }

    @Override
    public List<Playlist> getUserFavoritePlaylists(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return favoriteRepository.findByUserAndPlaylistIsNotNull(user)
                .stream()
                .map(Favourite::getPlaylist)
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> getUserFavoriteAlbums(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return favoriteRepository.findByUserAndAlbumIsNotNull(user)
                .stream()
                .map(Favourite::getAlbum)
                .collect(Collectors.toList());
    }

    @Override
    public List<Artist> getUserFavoriteArtists(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return favoriteRepository.findByUserAndArtistIsNotNull(user)
                .stream()
                .map(Favourite::getArtist)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addTrackToFavorites(Integer userId, Integer trackId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found with id: " + trackId));

        // Check if already in favorites
        if (isTrackInFavorites(userId, trackId)) {
            return false;
        }

        // Add to favorites
        Favourite favourite = Favourite.builder()
                .user(user)
                .track(track)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        favoriteRepository.save(favourite);
        return true;
    }

    @Override
    public boolean addPlaylistToFavorites(Integer userId, Integer playlistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found with id: " + playlistId));

        // Check if already in favorites
        if (isPlaylistInFavorites(userId, playlistId)) {
            return false;
        }

        // Add to favorites
        Favourite favourite = Favourite.builder()
                .user(user)
                .playlist(playlist)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        favoriteRepository.save(favourite);
        return true;
    }

    @Override
    public boolean addAlbumToFavorites(Integer userId, Integer albumId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with id: " + albumId));

        // Check if already in favorites
        if (isAlbumInFavorites(userId, albumId)) {
            return false;
        }

        // Add to favorites
        Favourite favourite = Favourite.builder()
                .user(user)
                .album(album)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        favoriteRepository.save(favourite);
        return true;
    }

    @Override
    public boolean addArtistToFavorites(Integer userId, Integer artistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + artistId));

        // Check if already in favorites
        if (isArtistInFavorites(userId, artistId)) {
            return false;
        }

        // Add to favorites
        Favourite favourite = Favourite.builder()
                .user(user)
                .artist(artist)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        favoriteRepository.save(favourite);
        return true;
    }

    @Override
    public boolean removeTrackFromFavorites(Integer userId, Integer trackId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found with id: " + trackId));

        Optional<Favourite> favoriteOpt = favoriteRepository.findByUserAndTrack(user, track);
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean removePlaylistFromFavorites(Integer userId, Integer playlistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found with id: " + playlistId));

        Optional<Favourite> favoriteOpt = favoriteRepository.findByUserAndPlaylist(user, playlist);
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAlbumFromFavorites(Integer userId, Integer albumId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with id: " + albumId));

        Optional<Favourite> favoriteOpt = favoriteRepository.findByUserAndAlbum(user, album);
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeArtistFromFavorites(Integer userId, Integer artistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + artistId));

        Optional<Favourite> favoriteOpt = favoriteRepository.findByUserAndArtist(user, artist);
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean isTrackInFavorites(Integer userId, Integer trackId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found with id: " + trackId));
        
        return favoriteRepository.findByUserAndTrack(user, track).isPresent();
    }

    @Override
    public boolean isPlaylistInFavorites(Integer userId, Integer playlistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found with id: " + playlistId));
        
        return favoriteRepository.findByUserAndPlaylist(user, playlist).isPresent();
    }

    @Override
    public boolean isAlbumInFavorites(Integer userId, Integer albumId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with id: " + albumId));
        
        return favoriteRepository.findByUserAndAlbum(user, album).isPresent();
    }

    @Override
    public boolean isArtistInFavorites(Integer userId, Integer artistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + artistId));
        
        return favoriteRepository.findByUserAndArtist(user, artist).isPresent();
    }
}
