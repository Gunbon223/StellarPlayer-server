package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Integer> {
    List<Artist> findByNameContainingIgnoreCase(String query);
    Optional<Artist> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Batch find artists by names for better performance
     */
    @Query("SELECT a FROM Artist a WHERE LOWER(a.name) IN :names")
    List<Artist> findByNameIgnoreCaseIn(@Param("names") List<String> names);
}
