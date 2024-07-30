package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Integer> {
}
