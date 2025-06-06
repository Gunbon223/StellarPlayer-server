package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    
}
