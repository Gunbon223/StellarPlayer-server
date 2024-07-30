package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Integer> {
}
