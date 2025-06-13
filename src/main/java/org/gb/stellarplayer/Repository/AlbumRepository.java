package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer> {
    List<Album> findByTitleContainingIgnoreCase(String query);
    Page<Album> findByArtistsIdOrderByCreatedAtDesc(int artistId, Pageable pageable);
    
    // Use custom query to avoid multiple results issue
    @Query("SELECT DISTINCT a FROM Album a JOIN a.artists ar WHERE LOWER(a.title) = LOWER(:title) AND ar IN :artists")
    Optional<Album> findByTitleIgnoreCaseAndArtistsIn(@Param("title") String title, @Param("artists") List<Artist> artists);
    
    @Query("SELECT COUNT(DISTINCT a) > 0 FROM Album a JOIN a.artists ar WHERE LOWER(a.title) = LOWER(:title) AND ar IN :artists")
    boolean existsByTitleIgnoreCaseAndArtistsIn(@Param("title") String title, @Param("artists") List<Artist> artists);
    
    // New methods for recommendations
    @Query("SELECT DISTINCT a FROM Album a JOIN a.artists ar WHERE ar = :artist ORDER BY a.createdAt DESC")
    List<Album> findByArtistsContaining(@Param("artist") Artist artist);
    
    @Query("SELECT DISTINCT t.album FROM Track t JOIN t.genres g WHERE g = :genre AND t.album IS NOT NULL ORDER BY t.album.createdAt DESC")
    List<Album> findByGenresContaining(@Param("genre") Genre genre);
    
    @Query("SELECT a FROM Album a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Album> findRecentAlbums(@Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT a FROM Album a WHERE a.releaseDate BETWEEN :startDate AND :endDate ORDER BY a.releaseDate DESC")
    List<Album> findByReleaseDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find albums by artist ID
     * @param artistId Artist ID
     * @return List of albums
     */
    @Query("SELECT a FROM Album a JOIN a.artists art WHERE art.id = :artistId")
    List<Album> findByArtistsId(@Param("artistId") Integer artistId);

    /**
     * Find albums by release date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of albums
     */


}
