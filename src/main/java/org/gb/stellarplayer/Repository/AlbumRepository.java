package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer> {
    List<Album> findByTitleContainingIgnoreCase(String query);
    Page<Album> findByArtistsIdOrderByCreatedAtDesc(int artistId, Pageable pageable);


}
