package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Model.Enum.PlaylistType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    List<Playlist> findByNameContainingIgnoreCase(String query);
    
    // User playlist methods
    List<Playlist> findByUserIdAndType(Integer userId, PlaylistType type);
    
    // Recommendation playlist methods
    Optional<Playlist> findByNameAndType(String name, PlaylistType type);
    Optional<Playlist> findByNameAndTypeAndUserId(String name, PlaylistType type, Integer userId);
    List<Playlist> findByTypeIn(List<PlaylistType> types);
    List<Playlist> findByTypeInAndUserId(List<PlaylistType> types, Integer userId);
    
    // Artist radio playlist method
    List<Playlist> findByNameAndUserId(String name, Integer userId);
}
