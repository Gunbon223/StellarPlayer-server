package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Genre entity
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    @Query("SELECT g FROM Genre g WHERE g.name = :name ORDER BY g.id ASC")
    Optional<Genre> findByName(@Param("name") String name);
    
    List<Genre> findByNameContainingIgnoreCase(String query);
    boolean existsByName(String name);
}
