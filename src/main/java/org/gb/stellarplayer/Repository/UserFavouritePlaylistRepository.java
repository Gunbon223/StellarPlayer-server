package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.UserFavouritePlaylist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavouritePlaylistRepository extends JpaRepository<UserFavouritePlaylist, Integer> {
    List<UserFavouritePlaylist> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<UserFavouritePlaylist> findByUserIdAndPlaylistId(Integer userId, Integer playlistId);
    boolean existsByUserIdAndPlaylistId(Integer userId, Integer playlistId);
    void deleteByUserIdAndPlaylistId(Integer userId, Integer playlistId);
    
    @Query("SELECT COUNT(f) FROM UserFavouritePlaylist f WHERE f.playlist.id = :playlistId")
    long countByPlaylistId(@Param("playlistId") Integer playlistId);
    
    @Query("SELECT f FROM UserFavouritePlaylist f JOIN FETCH f.playlist WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<UserFavouritePlaylist> findByUserIdWithPlaylistOrderByCreatedAtDesc(@Param("userId") Integer userId);
    
    // Paginated methods
    Page<UserFavouritePlaylist> findByUserId(Integer userId, Pageable pageable);
    
    @Query("SELECT f FROM UserFavouritePlaylist f JOIN FETCH f.playlist WHERE f.user.id = :userId")
    Page<UserFavouritePlaylist> findByUserIdWithPlaylist(@Param("userId") Integer userId, Pageable pageable);
} 