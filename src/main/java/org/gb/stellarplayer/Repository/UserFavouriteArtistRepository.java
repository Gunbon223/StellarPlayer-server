package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.UserFavouriteArtist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavouriteArtistRepository extends JpaRepository<UserFavouriteArtist, Integer> {
    List<UserFavouriteArtist> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<UserFavouriteArtist> findByUserIdAndArtistId(Integer userId, Integer artistId);
    boolean existsByUserIdAndArtistId(Integer userId, Integer artistId);
    void deleteByUserIdAndArtistId(Integer userId, Integer artistId);
    
    @Query("SELECT COUNT(f) FROM UserFavouriteArtist f WHERE f.artist.id = :artistId")
    long countByArtistId(@Param("artistId") Integer artistId);
    
    @Query("SELECT f FROM UserFavouriteArtist f JOIN FETCH f.artist WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<UserFavouriteArtist> findByUserIdWithArtistOrderByCreatedAtDesc(@Param("userId") Integer userId);
    
    // Paginated methods
    Page<UserFavouriteArtist> findByUserId(Integer userId, Pageable pageable);
    
    @Query("SELECT f FROM UserFavouriteArtist f JOIN FETCH f.artist WHERE f.user.id = :userId")
    Page<UserFavouriteArtist> findByUserIdWithArtist(@Param("userId") Integer userId, Pageable pageable);
} 