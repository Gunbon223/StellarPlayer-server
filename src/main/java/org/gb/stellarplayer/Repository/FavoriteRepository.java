package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favourite, Integer> {
    List<Favourite> findByUser(User user);
    List<Favourite> findByUserAndTrackIsNotNull(User user);
    List<Favourite> findByUserAndPlaylistIsNotNull(User user);
    List<Favourite> findByUserAndAlbumIsNotNull(User user);
    List<Favourite> findByUserAndArtistIsNotNull(User user);
    
    // New methods for finding specific favorites
    Optional<Favourite> findByUserAndTrack(User user, Track track);
    Optional<Favourite> findByUserAndPlaylist(User user, Playlist playlist);
    Optional<Favourite> findByUserAndAlbum(User user, Album album);
    Optional<Favourite> findByUserAndArtist(User user, Artist artist);
}
