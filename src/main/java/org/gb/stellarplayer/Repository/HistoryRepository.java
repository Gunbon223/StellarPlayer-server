package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.History;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.Playlist;
import org.gb.stellarplayer.Entites.Artist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {
    
    // Recently played tracks
    @Query("SELECT DISTINCT h.track FROM History h WHERE h.user.id = :userId AND h.track IS NOT NULL AND h.playedAt >= :since ORDER BY h.playedAt DESC")
    List<Track> findRecentlyPlayedTracks(@Param("userId") Integer userId, @Param("since") LocalDateTime since, Pageable pageable);
    
    // Recently played albums
    @Query("SELECT DISTINCT h.album FROM History h WHERE h.user.id = :userId AND h.album IS NOT NULL AND h.playedAt >= :since ORDER BY h.playedAt DESC")
    List<Album> findRecentlyPlayedAlbums(@Param("userId") Integer userId, @Param("since") LocalDateTime since, Pageable pageable);
    
    // Recently played playlists
    @Query("SELECT DISTINCT h.playlist FROM History h WHERE h.user.id = :userId AND h.playlist IS NOT NULL AND h.playedAt >= :since ORDER BY h.playedAt DESC")
    List<Playlist> findRecentlyPlayedPlaylists(@Param("userId") Integer userId, @Param("since") LocalDateTime since, Pageable pageable);
    
    // Recently played artists (from tracks)
    @Query("SELECT DISTINCT a FROM History h JOIN h.track.artists a WHERE h.user.id = :userId AND h.track IS NOT NULL AND h.playedAt >= :since ORDER BY h.playedAt DESC")
    List<Artist> findRecentlyPlayedArtists(@Param("userId") Integer userId, @Param("since") LocalDateTime since, Pageable pageable);
    
    // Recently played artists (direct artist plays)
    @Query("SELECT DISTINCT h.artist FROM History h WHERE h.user.id = :userId AND h.artist IS NOT NULL AND h.playedAt >= :since ORDER BY h.playedAt DESC")
    List<Artist> findRecentlyPlayedArtistsDirectly(@Param("userId") Integer userId, @Param("since") LocalDateTime since, Pageable pageable);
    
    // Get all history for a user in date range
    @Query("SELECT h FROM History h WHERE h.user.id = :userId AND h.playedAt >= :since ORDER BY h.playedAt DESC")
    List<History> findUserHistorySince(@Param("userId") Integer userId, @Param("since") LocalDateTime since, Pageable pageable);
    
    // Find most recent play of a specific track by user
    @Query("SELECT h FROM History h WHERE h.user.id = :userId AND h.track.id = :trackId ORDER BY h.playedAt DESC")
    List<History> findUserTrackHistory(@Param("userId") Integer userId, @Param("trackId") Integer trackId, Pageable pageable);
    
    // Find existing history records for upsert operations - simpler approach
    List<History> findByUserIdAndTrackId(Integer userId, Integer trackId);
    List<History> findByUserIdAndAlbumId(Integer userId, Integer albumId);
    List<History> findByUserIdAndPlaylistId(Integer userId, Integer playlistId);
    List<History> findByUserIdAndArtistId(Integer userId, Integer artistId);
    
    // Cleanup queries to optimize database storage
    @Modifying
    @Transactional
    @Query("DELETE FROM History h WHERE h.playedAt < :cutoffDate")
    int deleteHistoryOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM History h WHERE h.user.id = :userId AND h.playedAt < :cutoffDate")
    int deleteUserHistoryOlderThan(@Param("userId") Integer userId, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(h) FROM History h")
    long countAllHistory();
    
    @Query("SELECT COUNT(h) FROM History h WHERE h.playedAt < :cutoffDate")
    long countHistoryOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(h) FROM History h WHERE h.user.id = :userId")
    long countUserHistory(@Param("userId") Integer userId);
    
    @Query("SELECT COUNT(h) FROM History h WHERE h.playedAt >= :since")
    long countRecentHistory(@Param("since") LocalDateTime since);
    
    // Find oldest and newest history records for statistics
    @Query("SELECT h FROM History h ORDER BY h.playedAt ASC")
    List<History> findOldestHistory(Pageable pageable);
    
    @Query("SELECT h FROM History h ORDER BY h.playedAt DESC")
    List<History> findNewestHistory(Pageable pageable);

    /**
     * Delete all history entries for a specific playlist
     * @param playlistId Playlist ID
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM History h WHERE h.playlist.id = :playlistId")
    int deleteByPlaylistId(@Param("playlistId") Integer playlistId);

    /**
     * Delete all history entries for a specific album
     * @param albumId Album ID
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM History h WHERE h.album.id = :albumId")
    int deleteByAlbumId(@Param("albumId") Integer albumId);

    /**
     * Delete all history entries for a specific track
     * @param trackId Track ID
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM History h WHERE h.track.id = :trackId")
    int deleteByTrackId(@Param("trackId") Integer trackId);

    /**
     * Delete all history entries for a specific user
     * @param userId User ID
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM History h WHERE h.user.id = :userId")
    int deleteByUserId(@Param("userId") Integer userId);
}
