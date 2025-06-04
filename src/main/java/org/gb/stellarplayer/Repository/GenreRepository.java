package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Genre entity
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findByName(String name);
    List<Genre> findByNameContainingIgnoreCase(String query);
    boolean existsByName(String name);
}
