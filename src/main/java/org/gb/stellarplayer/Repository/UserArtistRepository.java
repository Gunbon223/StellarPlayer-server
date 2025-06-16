package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.UserArtist;
import org.gb.stellarplayer.Entites.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserArtistRepository extends JpaRepository<UserArtist, Integer> {
    
    @Query("SELECT ua FROM UserArtist ua WHERE ua.user.id = :userId")
    List<UserArtist> findByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT ua FROM UserArtist ua WHERE ua.artist.id = :artistId")
    List<UserArtist> findByArtistId(@Param("artistId") Integer artistId);
    
    @Query("SELECT ua FROM UserArtist ua WHERE ua.user.id = :userId AND ua.artist.id = :artistId")
    Optional<UserArtist> findByUserIdAndArtistId(@Param("userId") Integer userId, @Param("artistId") Integer artistId);
    
    @Query("SELECT ua.artist FROM UserArtist ua WHERE ua.user.id = :userId")
    List<Artist> findArtistsByUserId(@Param("userId") Integer userId);
    
    boolean existsByUserIdAndArtistId(Integer userId, Integer artistId);
    
    void deleteByUserIdAndArtistId(Integer userId, Integer artistId);
    
    // Fetch with join to avoid lazy loading issues
    @Query("SELECT ua FROM UserArtist ua JOIN FETCH ua.user JOIN FETCH ua.artist")
    List<UserArtist> findAllWithUserAndArtist();
    
    @Query("SELECT ua FROM UserArtist ua JOIN FETCH ua.user JOIN FETCH ua.artist WHERE ua.user.id = :userId")
    List<UserArtist> findByUserIdWithUserAndArtist(@Param("userId") Integer userId);
    
    @Query("SELECT ua FROM UserArtist ua JOIN FETCH ua.user JOIN FETCH ua.artist WHERE ua.artist.id = :artistId")
    List<UserArtist> findByArtistIdWithUserAndArtist(@Param("artistId") Integer artistId);
} 