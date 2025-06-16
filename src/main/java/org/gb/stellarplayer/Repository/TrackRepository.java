package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.Artist;
import org.gb.stellarplayer.Entites.Genre;
import org.gb.stellarplayer.Entites.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {
   List<Track> findByAlbumId(int id);
   List<Track> findByTitleContainingIgnoreCase(String query);
   Page<Track> findByArtistsIdOrderByCreatedAtDesc(int artistId, Pageable pageable);
   
   /**
    * Find all tracks by genre
    * @param genre The genre to search for
    * @return List of tracks with the given genre
    */
   List<Track> findByGenresContaining(Genre genre);
   
   /**
    * Find random tracks by genre, limited by count
    * @param genre The genre to search for
    * @param pageable Pagination information
    * @return Page of tracks with the given genre
    */
   @Query(value = "SELECT t.* FROM track t " +
           "JOIN track_genre tg ON t.id = tg.track_id " +
           "WHERE tg.genre_id = :genreId " +
           "ORDER BY RAND() " +
           "LIMIT :limit", nativeQuery = true)
   List<Track> findRandomTracksByGenreId(@Param("genreId") int genreId, @Param("limit") int limit);
   
   /**
    * Count tracks by genre
    * @param genre The genre to count
    * @return Number of tracks with the given genre
    */
   @Query("SELECT COUNT(t) FROM Track t JOIN t.genres g WHERE g = :genre")
   long countByGenre(@Param("genre") Genre genre);
   
   // Additional methods for recommendation system
   List<Track> findByStatusTrue();
   
   List<Track> findByStatusTrueOrderByPlayCountDesc();
   
   List<Track> findByStatusTrueAndCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);
   
   Page<Track> findByStatusFalse(Pageable pageable);
   
   @Query(value = "SELECT DISTINCT t.* FROM track t " +
           "INNER JOIN track_genre tg ON t.id = tg.track_id " +
           "WHERE tg.genre_id IN :genreIds AND t.status = true " +
           "ORDER BY t.play_count DESC",
           nativeQuery = true)
   List<Track> findByGenreIdsAndStatusTrue(@Param("genreIds") List<Integer> genreIds);
   
   @Query(value = "SELECT DISTINCT t.* FROM track t " +
           "INNER JOIN track_artist ta ON t.id = ta.track_id " +
           "WHERE ta.artist_id IN :artistIds AND t.status = true " +
           "ORDER BY t.play_count DESC",
           nativeQuery = true)
   List<Track> findByArtistIdsAndStatusTrue(@Param("artistIds") List<Integer> artistIds);
   
   // Check if track exists with same title and artists
   @Query("SELECT COUNT(t) > 0 FROM Track t JOIN t.artists a WHERE LOWER(t.title) = LOWER(:title) AND a IN :artists")
   boolean existsByTitleIgnoreCaseAndArtistsIn(@Param("title") String title, @Param("artists") List<Artist> artists);
   
   // Find tracks with same title and artists for detailed checking
   @Query("SELECT DISTINCT t FROM Track t JOIN t.artists a WHERE LOWER(t.title) = LOWER(:title) AND a IN :artists")
   List<Track> findByTitleIgnoreCaseAndArtistsIn(@Param("title") String title, @Param("artists") List<Artist> artists);
   
   // Optimized duplicate check with early termination - returns Long (0 or 1) to avoid casting issues
   @Query(value = "SELECT EXISTS(SELECT 1 FROM track t JOIN track_artist ta ON t.id = ta.track_id JOIN artist a ON ta.artist_id = a.id WHERE LOWER(t.title) = LOWER(:title) AND LOWER(a.name) IN :artistNames LIMIT 1)", nativeQuery = true)
   Long existsByTitleAndArtistNamesOptimizedRaw(@Param("title") String title, @Param("artistNames") List<String> artistNames);
   
   // Alternative JPQL version for boolean return
   @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Track t JOIN t.artists a WHERE LOWER(t.title) = LOWER(:title) AND LOWER(a.name) IN :artistNames")
   boolean existsByTitleAndArtistNamesOptimized(@Param("title") String title, @Param("artistNames") List<String> artistNames);
   
   // Batch find tracks by artist names for better performance
   @Query("SELECT DISTINCT t FROM Track t JOIN t.artists a WHERE LOWER(a.name) IN :artistNames")
   List<Track> findByArtistNamesIn(@Param("artistNames") List<String> artistNames);
   
   // Check if track exists by title and artist names (simpler approach)
   @Query("SELECT COUNT(t) > 0 FROM Track t JOIN t.artists a WHERE LOWER(t.title) = LOWER(:title) AND LOWER(a.name) IN :artistNames")
   boolean existsByTitleAndArtistNames(@Param("title") String title, @Param("artistNames") List<String> artistNames);
   
   // Find tracks by title and artist names for detailed checking
   @Query("SELECT t FROM Track t WHERE LOWER(t.title) = LOWER(:title) AND EXISTS (SELECT 1 FROM t.artists a WHERE LOWER(a.name) IN :artistNames)")
   List<Track> findByTitleAndArtistNames(@Param("title") String title, @Param("artistNames") List<String> artistNames);
   
   // Get first track by title and artist names (to avoid multiple results issue)
   @Query(value = "SELECT t.* FROM track t WHERE LOWER(t.title) = LOWER(:title) AND EXISTS (SELECT 1 FROM track_artist ta JOIN artist a ON ta.artist_id = a.id WHERE ta.track_id = t.id AND LOWER(a.name) IN :artistNames) LIMIT 1", nativeQuery = true)
   Track findFirstByTitleAndArtistNames(@Param("title") String title, @Param("artistNames") List<String> artistNames);
   
   // Recommendation system methods
   @Query("SELECT t FROM Track t WHERE t.createdAt >= :cutoffDate AND t.status = true ORDER BY t.playCount DESC")
   List<Track> findTrendingTracks(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

   @Query("SELECT t FROM Track t WHERE t.releaseDate >= :cutoffDate AND t.status = true ORDER BY t.releaseDate DESC")
   List<Track> findNewTracks(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

   @Query("SELECT t FROM Track t WHERE t.lastPlayedAt >= :cutoffDate AND t.status = true ORDER BY t.playCount DESC")
   List<Track> findViralTracks(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

   @Query(value = "SELECT DISTINCT t.* FROM track t " +
           "LEFT JOIN track_genre tg ON t.id = tg.track_id " +
           "LEFT JOIN track_artist ta ON t.id = ta.track_id " +
           "WHERE t.status = true " +
           "AND (tg.genre_id IN :genreIds OR ta.artist_id IN :artistIds) " +
           "AND t.id NOT IN :excludeIds " +
           "ORDER BY t.play_count DESC", nativeQuery = true)
   List<Track> findRecommendedTracks(@Param("genreIds") List<Integer> genreIds, @Param("artistIds") List<Integer> artistIds, @Param("excludeIds") List<Integer> excludeIds, Pageable pageable);

   List<Track> findByArtistsContaining(Artist artist);
   
   // Additional method for discovery weekly fallback
   @Query("SELECT t FROM Track t WHERE t.status = true ORDER BY t.createdAt DESC")
   List<Track> findAllByStatusTrue(Pageable pageable);
}
