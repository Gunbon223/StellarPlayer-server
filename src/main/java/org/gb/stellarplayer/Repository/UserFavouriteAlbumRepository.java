package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.UserFavouriteAlbum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavouriteAlbumRepository extends JpaRepository<UserFavouriteAlbum, Integer> {
    List<UserFavouriteAlbum> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<UserFavouriteAlbum> findByUserIdAndAlbumId(Integer userId, Integer albumId);
    boolean existsByUserIdAndAlbumId(Integer userId, Integer albumId);
    void deleteByUserIdAndAlbumId(Integer userId, Integer albumId);
    
    @Query("SELECT COUNT(f) FROM UserFavouriteAlbum f WHERE f.album.id = :albumId")
    long countByAlbumId(@Param("albumId") Integer albumId);
    
    @Query("SELECT f FROM UserFavouriteAlbum f JOIN FETCH f.album WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<UserFavouriteAlbum> findByUserIdWithAlbumOrderByCreatedAtDesc(@Param("userId") Integer userId);
    
    // Paginated methods
    Page<UserFavouriteAlbum> findByUserId(Integer userId, Pageable pageable);
    
    @Query("SELECT f FROM UserFavouriteAlbum f JOIN FETCH f.album WHERE f.user.id = :userId")
    Page<UserFavouriteAlbum> findByUserIdWithAlbum(@Param("userId") Integer userId, Pageable pageable);

    /**
     * Delete all user favorites for a specific album
     * @param albumId Album ID
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM UserFavouriteAlbum ufa WHERE ufa.album.id = :albumId")
    int deleteByAlbumId(@Param("albumId") Integer albumId);

}