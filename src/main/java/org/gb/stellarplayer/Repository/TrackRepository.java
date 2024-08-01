package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Integer> {
   List<Track> findByAlbumId(int id);
}
