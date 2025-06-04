package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
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
}
