package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.Model.Enum.PlaylistType;
import org.gb.stellarplayer.Repository.*;
import org.gb.stellarplayer.Response.AlbumSearchDTO;
import org.gb.stellarplayer.Response.PlaylistSearchDTO;
import org.gb.stellarplayer.DTO.ArtistRadioPlaylistDTO;
import org.gb.stellarplayer.Service.RecommendationService;
import org.gb.stellarplayer.Service.PlaylistCoverGeneratorService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final UserTrackInteractionRepository userTrackInteractionRepository;
    private final UserFavouriteArtistRepository userFavouriteArtistRepository;
    private final UserFavouriteTrackRepository userFavouriteTrackRepository;
    private final UserFavouritePlaylistRepository userFavouritePlaylistRepository;
    private final PlaylistCoverGeneratorService coverGeneratorService;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final HistoryRepository historyRepository;
    
    @Override
    public Playlist generateTrendingWeekly() {
        List<Track> trendingTracks = getTrendingTracks(50);
        
        // Find existing trending playlist or create new one
        List<Playlist> existingPlaylists = playlistRepository.findByTypeIn(
            Arrays.asList(PlaylistType.TRENDING));
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty()) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(trendingTracks);
            playlist.setUpdatedAt(LocalDateTime.now());
        } else {
            playlist = Playlist.builder()
                .name("Trending This Week")
                .type(PlaylistType.TRENDING)
                .status(true)
                .cover("https://res.cloudinary.com/dll5rlqx9/image/upload/v1749388951/playlist-cover/diamond-cover_jxrhla.png")
                .tracks(trendingTracks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        // Set playlist cover from first track or use default with text overlay
        setPlaylistCoverFromFirstTrack(playlist);
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public Playlist generateNewMusicDaily() {
        List<Track> newTracks = getNewTracks(1, 30); // Last 1 day, 30 tracks
        
        List<Playlist> existingPlaylists = playlistRepository.findByTypeIn(
            Arrays.asList(PlaylistType.NEW_DAILY));
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty()) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(newTracks);
            playlist.setUpdatedAt(LocalDateTime.now());
                } else {
            playlist = Playlist.builder()
                .name("New Music Daily")
                .type(PlaylistType.NEW_DAILY)
                .status(true)
                .cover("https://res.cloudinary.com/dll5rlqx9/image/upload/v1749388949/playlist-cover/output_nkr5nu.jpg")
                .tracks(newTracks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        // Set playlist cover from first track or use default with text overlay
        setPlaylistCoverFromFirstTrack(playlist);

        return playlistRepository.save(playlist);
    }
    
    @Override
    public Playlist generateNewReleases() {
        List<Track> newReleases = getNewTracks(7, 40); // Last 7 days, 40 tracks
        
        List<Playlist> existingPlaylists = playlistRepository.findByTypeIn(
            Arrays.asList(PlaylistType.NEW_RELEASE));
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty()) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(newReleases);
            playlist.setUpdatedAt(LocalDateTime.now());
        } else {
            playlist = Playlist.builder()
                .name("New Music Releases")
                .type(PlaylistType.NEW_RELEASE)
                .status(true)
                .cover("https://res.cloudinary.com/dll5rlqx9/image/upload/v1749388950/playlist-cover/download_uqequ1.jpg")
                .tracks(newReleases)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public Playlist generateViralHits() {
        List<Track> viralTracks = getViralTracks(35);
        
        List<Playlist> existingPlaylists = playlistRepository.findByTypeIn(
            Arrays.asList(PlaylistType.VIRAL));
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty()) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(viralTracks);
            playlist.setUpdatedAt(LocalDateTime.now());
        } else {
            playlist = Playlist.builder()
                .name("Viral Hits")
                .type(PlaylistType.VIRAL)
                .status(true)
                .cover("https://res.cloudinary.com/dll5rlqx9/image/upload/v1749388950/playlist-cover/download_uqequ1.jpg")
                .tracks(viralTracks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public Playlist generateUserDiscoveryWeekly(Integer userId) {
        List<Track> recommendedTracks = getRecommendedTracksForUser(userId, 30);
        
        // If no recommended tracks found, get newer tracks as fallback
        if (recommendedTracks.isEmpty()) {
            // Try getting tracks from last 30 days first
            recommendedTracks = getNewTracks(30, 30);
            
            // If still empty, get tracks from last 90 days
            if (recommendedTracks.isEmpty()) {
                recommendedTracks = getNewTracks(90, 30);
            }
            
            // Last resort: get any available tracks
            if (recommendedTracks.isEmpty()) {
                Pageable pageable = PageRequest.of(0, 30);
                recommendedTracks = trackRepository.findAllByStatusTrue(pageable);
            }
        }
        
        String playlistName = "Discover Weekly";
        
        // Find existing user discovery playlist
        List<Playlist> existingPlaylists = playlistRepository.findByTypeInAndUserId(
            Arrays.asList(PlaylistType.USER_REC), userId);
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty()) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(recommendedTracks);
            playlist.setUpdatedAt(LocalDateTime.now());
        } else {
            User user = userRepository.findById(userId).orElse(null);
            playlist = Playlist.builder()
                .name(playlistName)
                .type(PlaylistType.USER_REC)
                .status(true)
                .cover("https://res.cloudinary.com/dll5rlqx9/image/upload/v1749389347/playlist-cover/discovery_mu93py.jpg")
                .user(user)
                .tracks(recommendedTracks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public Playlist generateUserArtistMix(Integer userId) {
        // Get user's favorite artists
        List<UserFavouriteArtist> favouriteArtists = userFavouriteArtistRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
        
        if (favouriteArtists.isEmpty()) {
            return null; // No favorite artists to base recommendations on
        }
        
        // Get tracks from favorite artists
        List<Track> artistTracks = new ArrayList<>();
        for (UserFavouriteArtist favArtist : favouriteArtists.subList(0, Math.min(5, favouriteArtists.size()))) {
            List<Track> tracks = trackRepository.findByArtistsContaining(favArtist.getArtist());
            artistTracks.addAll(tracks.subList(0, Math.min(6, tracks.size())));
        }
        
        // Shuffle and limit
        Collections.shuffle(artistTracks);
        List<Track> finalTracks = artistTracks.subList(0, Math.min(30, artistTracks.size()));
        
        String playlistName = "Your Artist Mix";
        List<Playlist> existingPlaylists = playlistRepository.findByTypeInAndUserId(
            Arrays.asList(PlaylistType.ARTIST_MIX), userId);
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty()) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(finalTracks);
            playlist.setUpdatedAt(LocalDateTime.now());
        } else {
            User user = userRepository.findById(userId).orElse(null);
            playlist = Playlist.builder()
                .name(playlistName)
                .type(PlaylistType.ARTIST_MIX)
                .status(true)
                .cover("https://res.cloudinary.com/dll5rlqx9/image/upload/v1749306649/covers/cvyticqio1ysjyphaxof.jpg")
                .user(user)
                .tracks(finalTracks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public Playlist generateUserGenreMix(Integer userId) {
        // Get user's listening history to determine preferred genres
        Pageable pageable = PageRequest.of(0, 50);
        List<UserTrackInteraction> userInteractions = userTrackInteractionRepository
            .findTopByUserIdOrderByInteractionScoreDesc(userId, pageable);
        
        // Count genre preferences based on interaction scores
        Map<Genre, Double> genreScores = new HashMap<>();
        for (UserTrackInteraction interaction : userInteractions) {
            Track track = interaction.getTrack();
            if (track.getGenres() != null) {
                for (Genre genre : track.getGenres()) {
                    genreScores.merge(genre, interaction.getInteractionScore(), Double::sum);
                }
            }
        }
        
        // Get top genres
        List<Genre> topGenres = genreScores.entrySet().stream()
            .sorted(Map.Entry.<Genre, Double>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        if (topGenres.isEmpty()) {
            return null;
        }
        
        // Get tracks from preferred genres
        List<Track> genreTracks = new ArrayList<>();
        for (Genre genre : topGenres) {
            List<Track> tracks = trackRepository.findRandomTracksByGenreId(genre.getId(), 10);
            genreTracks.addAll(tracks);
        }
        
        Collections.shuffle(genreTracks);
        List<Track> finalTracks = genreTracks.subList(0, Math.min(30, genreTracks.size()));
        
        String playlistName = "Your Genre Mix";
        List<Playlist> existingPlaylists = playlistRepository.findByTypeInAndUserId(
            Arrays.asList(PlaylistType.GENRE_MIX), userId);
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty()) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(finalTracks);
            playlist.setUpdatedAt(LocalDateTime.now());
        } else {
            User user = userRepository.findById(userId).orElse(null);
            playlist = Playlist.builder()
                .name(playlistName)
                .type(PlaylistType.GENRE_MIX)
                .status(true)
                .cover("https://res.cloudinary.com/dll5rlqx9/image/upload/v1749306625/covers/gqpz9latmyqlkw68hu6s.jpg")
                .user(user)
                .tracks(finalTracks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public List<Track> getTrendingTracks(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(0, limit);
        return trackRepository.findTrendingTracks(weekAgo, pageable);
    }
    
    @Override
    public List<Track> getNewTracks(int days, int limit) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);
        return trackRepository.findNewTracks(cutoffDate, pageable);
    }
    
    @Override
    public List<Track> getViralTracks(int limit) {
        // Tracks with rapidly increasing play counts in recent days
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        Pageable pageable = PageRequest.of(0, limit);
        return trackRepository.findViralTracks(threeDaysAgo, pageable);
    }
    
    @Override
    public List<Track> getRecommendedTracksForUser(Integer userId, int limit) {
        // Get tracks similar to user's high-scored interactions
        Pageable pageable = PageRequest.of(0, 20);
        List<UserTrackInteraction> topInteractions = userTrackInteractionRepository
            .findTopByUserIdOrderByInteractionScoreDesc(userId, pageable);
        
        if (topInteractions.isEmpty()) {
            // Fallback to trending tracks for new users
            return getTrendingTracks(limit);
        }
        
        // Get genre and artist IDs from top interactions
        List<Integer> preferredGenreIds = new ArrayList<>();
        List<Integer> preferredArtistIds = new ArrayList<>();
        
        for (UserTrackInteraction interaction : topInteractions) {
            Track track = interaction.getTrack();
            if (track.getGenres() != null) {
                for (Genre genre : track.getGenres()) {
                    if (!preferredGenreIds.contains(genre.getId())) {
                        preferredGenreIds.add(genre.getId());
                    }
                }
            }
            if (track.getArtists() != null) {
                for (Artist artist : track.getArtists()) {
                    if (!preferredArtistIds.contains(artist.getId())) {
                        preferredArtistIds.add(artist.getId());
                    }
                }
            }
        }
        
        // Get track IDs to exclude
        List<Integer> excludeTrackIds = topInteractions.stream()
            .map(interaction -> interaction.getTrack().getId())
            .collect(Collectors.toList());
        
        Pageable limitPageable = PageRequest.of(0, limit);
        List<Track> recommendations = trackRepository
            .findRecommendedTracks(preferredGenreIds, preferredArtistIds, excludeTrackIds, limitPageable);
        
        return recommendations;
    }
    
    @Override
    public void updateSystemPlaylists() {
        generateTrendingWeekly();
        generateNewMusicDaily();
        generateNewReleases();
        generateViralHits();
    }
    
    @Override
    public void updateUserPlaylists(Integer userId) {
        generateUserDiscoveryWeekly(userId);
        generateUserArtistMix(userId);
        generateUserGenreMix(userId);
    }
    
    @Override
    public List<Playlist> getRecommendationPlaylists() {
        return playlistRepository.findByTypeIn(Arrays.asList(
            PlaylistType.TRENDING,
            PlaylistType.NEW_DAILY,
            PlaylistType.NEW_RELEASE,
            PlaylistType.VIRAL
        ));
    }
    
    @Override
    public List<Playlist> getUserRecommendationPlaylists(Integer userId) {
        return playlistRepository.findByTypeInAndUserId(Arrays.asList(
            PlaylistType.USER_REC,
            PlaylistType.ARTIST_MIX,
            PlaylistType.GENRE_MIX
        ), userId);
    }
    
    @Override
    public List<AlbumSearchDTO> getRecommendedAlbumsBasedOnHistory(Integer userId, int limit) {
        // Get user's listening history
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Pageable historyPageable = PageRequest.of(0, 100);
        List<History> recentHistory = historyRepository.findUserHistorySince(userId, thirtyDaysAgo, historyPageable);
        
        // Extract artists from history
        Map<Artist, Long> artistPlayCounts = new HashMap<>();
        Set<Album> userListenedAlbums = new HashSet<>();
        
        for (History history : recentHistory) {
            if (history.getTrack() != null && history.getTrack().getArtists() != null) {
                for (Artist artist : history.getTrack().getArtists()) {
                    artistPlayCounts.merge(artist, 1L, Long::sum);
                }
                
                // Track albums user has already listened to
                if (history.getTrack().getAlbum() != null) {
                    userListenedAlbums.add(history.getTrack().getAlbum());
                }
            }
        }
        
        // Get user's favorite artists
        List<UserFavouriteArtist> favoriteArtists = userFavouriteArtistRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
        
        // Add favorite artists to the play count map
        for (UserFavouriteArtist favoriteArtist : favoriteArtists) {
            artistPlayCounts.merge(favoriteArtist.getArtist(), 10L, Long::sum); // Give favorites higher weight
        }
        
        if (artistPlayCounts.isEmpty()) {
            // Fallback to trending albums for new users
            Pageable albumPageable = PageRequest.of(0, limit);
            return albumRepository.findRecentAlbums(LocalDateTime.now().minusDays(30), albumPageable).stream()
                .map(album -> AlbumSearchDTO.builder()
                    .id(album.getId())
                    .title(album.getTitle())
                    .cover(album.getCover())
                    .artistNames(album.getArtists() != null ? album.getArtists().stream().map(artist -> artist.getName()).toList() : List.of())
                    .releaseDate(album.getReleaseDate() != null ? album.getReleaseDate().toString() : null)
                    .build())
                .collect(Collectors.toList());
        }
        
        // Get top artists by play count
        List<Artist> topArtists = artistPlayCounts.entrySet().stream()
            .sorted(Map.Entry.<Artist, Long>comparingByValue().reversed())
            .limit(8)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // Get albums from these artists and similar artists
        Set<Album> recommendedAlbums = new HashSet<>();
        
        for (Artist artist : topArtists) {
            // Get albums by this artist
            List<Album> artistAlbums = albumRepository.findByArtistsContaining(artist);
            
            // Filter out albums user has already listened to extensively
            artistAlbums = artistAlbums.stream()
                .filter(album -> !userListenedAlbums.contains(album))
                .limit(2) // Limit per artist to ensure variety
                .collect(Collectors.toList());
            
            recommendedAlbums.addAll(artistAlbums);
            
            if (recommendedAlbums.size() >= limit) {
                break;
            }
        }
        
        // If not enough albums, fill with albums from similar genres
        if (recommendedAlbums.size() < limit) {
            Set<Genre> userGenres = new HashSet<>();
            for (History history : recentHistory) {
                if (history.getTrack() != null && history.getTrack().getGenres() != null) {
                    userGenres.addAll(history.getTrack().getGenres());
                }
            }
            
            for (Genre genre : userGenres) {
                if (recommendedAlbums.size() >= limit) break;
                
                List<Album> genreAlbums = albumRepository.findByGenresContaining(genre);
                genreAlbums = genreAlbums.stream()
                    .filter(album -> !userListenedAlbums.contains(album))
                    .filter(album -> !recommendedAlbums.contains(album))
                    .limit(3)
                    .collect(Collectors.toList());
                
                recommendedAlbums.addAll(genreAlbums);
            }
        }
        
        // Convert to list and shuffle
        List<Album> finalRecommendations = new ArrayList<>(recommendedAlbums);
        Collections.shuffle(finalRecommendations);
        
        return finalRecommendations.stream()
            .limit(limit)
            .map(album -> AlbumSearchDTO.builder()
                .id(album.getId())
                .title(album.getTitle())
                .cover(album.getCover())
                .artistNames(album.getArtists() != null ? album.getArtists().stream().map(artist -> artist.getName()).toList() : List.of())
                .releaseDate(album.getReleaseDate() != null ? album.getReleaseDate().toString() : null)
                .build())
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ArtistRadioPlaylistDTO> getArtistRadioPlaylists(Integer userId, int limit) {
        // Auto-generate artist radio playlists if not enough exist or if they need refresh
        autoGenerateArtistRadioPlaylists(userId, limit);
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        
        return playlistRepository.findByTypeInAndUserId(
            Arrays.asList(PlaylistType.ARTIST_RADIO), userId)
            .stream()
            .limit(limit)
            .map(playlist -> {
                String artistName = playlist.getName().replace(" Radio", "");
                boolean needsRefresh = playlist.getUpdatedAt() == null || 
                    playlist.getUpdatedAt().isBefore(oneWeekAgo);
                
                // Try to find artist ID from the playlist name
                Integer artistId = null;
                List<Artist> artists = artistRepository.findByNameContainingIgnoreCase(artistName);
                if (!artists.isEmpty()) {
                    artistId = artists.get(0).getId();
                }
                
                return ArtistRadioPlaylistDTO.builder()
                    .id(playlist.getId())
                    .name(playlist.getName())
                    .cover(playlist.getCover())
                    .trackCount(playlist.getTracks() != null ? playlist.getTracks().size() : 0)
                    .artistName(artistName)
                    .lastUpdated(playlist.getUpdatedAt())
                    .needsRefresh(needsRefresh)
                    .artistId(artistId)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Playlist generateArtistRadio(Integer userId, Integer artistId) {
        Artist artist = artistRepository.findById(artistId).orElse(null);
        if (artist == null) {
            return null;
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        String playlistName = artist.getName() + " Radio";
        
        // Check if playlist already exists and if it needs refresh (older than 1 week)
        List<Playlist> existingPlaylists = playlistRepository.findByNameAndUserId(playlistName, userId);
        boolean needsRefresh = false;
        
        if (!existingPlaylists.isEmpty()) {
            Playlist existingPlaylist = existingPlaylists.get(0);
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            if (existingPlaylist.getUpdatedAt() == null || existingPlaylist.getUpdatedAt().isBefore(oneWeekAgo)) {
                needsRefresh = true;
            } else {
                // Return existing playlist if it's fresh (less than 1 week old)
                return existingPlaylist;
            }
        }
        
        // Generate random track count between 20-30
        int targetTrackCount = 20 + (int)(Math.random() * 11); // 20-30 tracks
        
        // Get tracks by this artist (60% of playlist)
        List<Track> artistTracks = trackRepository.findByArtistsContaining(artist);
        Collections.shuffle(artistTracks);
        int artistTrackCount = (int)(targetTrackCount * 0.6);
        List<Track> selectedArtistTracks = artistTracks.stream()
            .limit(artistTrackCount)
            .collect(Collectors.toList());
        
        // Get tracks from similar genres and artists (40% of playlist)
        Set<Genre> artistGenres = new HashSet<>();
        for (Track track : artistTracks) {
            if (track.getGenres() != null) {
                artistGenres.addAll(track.getGenres());
            }
        }
        
        // Get user's listening history to find similar artists
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Pageable historyPageable = PageRequest.of(0, 100);
        List<History> recentHistory = historyRepository.findUserHistorySince(userId, thirtyDaysAgo, historyPageable);
        
        Set<Artist> relatedArtists = new HashSet<>();
        for (History history : recentHistory) {
            if (history.getTrack() != null && history.getTrack().getArtists() != null) {
                for (Artist trackArtist : history.getTrack().getArtists()) {
                    // Add artists from user's history that share genres with the main artist
                    if (!trackArtist.equals(artist)) {
                        List<Track> trackArtistTracks = trackRepository.findByArtistsContaining(trackArtist);
                        for (Track t : trackArtistTracks) {
                            if (t.getGenres() != null && 
                                t.getGenres().stream().anyMatch(artistGenres::contains)) {
                                relatedArtists.add(trackArtist);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        List<Track> genreAndSimilarTracks = new ArrayList<>();
        int remainingTracks = targetTrackCount - selectedArtistTracks.size();
        
        // Get tracks from related artists (20% of total)
        int relatedArtistCount = Math.min((int)(targetTrackCount * 0.2), remainingTracks / 2);
        List<Artist> relatedArtistsList = new ArrayList<>(relatedArtists);
        Collections.shuffle(relatedArtistsList);
        
        for (Artist relatedArtist : relatedArtistsList.stream().limit(relatedArtistCount > 0 ? relatedArtistCount / 2 : 1).collect(Collectors.toList())) {
            List<Track> relatedTracks = trackRepository.findByArtistsContaining(relatedArtist);
            Collections.shuffle(relatedTracks);
            genreAndSimilarTracks.addAll(relatedTracks.stream()
                .filter(track -> !track.getArtists().contains(artist))
                .limit(2)
                .collect(Collectors.toList()));
        }
        
        // Fill remaining with genre-based tracks (20% of total)
        int genreTrackCount = remainingTracks - genreAndSimilarTracks.size();
        for (Genre genre : artistGenres) {
            if (genreAndSimilarTracks.size() >= remainingTracks) break;
            
            List<Track> tracksInGenre = trackRepository.findRandomTracksByGenreId(genre.getId(), 
                Math.max(1, genreTrackCount / artistGenres.size()));
            genreAndSimilarTracks.addAll(tracksInGenre.stream()
                .filter(track -> !track.getArtists().contains(artist))
                .filter(track -> genreAndSimilarTracks.stream().noneMatch(t -> t.getId().equals(track.getId())))
                .limit(Math.max(1, genreTrackCount / artistGenres.size()))
                .collect(Collectors.toList()));
        }
        
        // Combine and shuffle all tracks
        List<Track> finalTracks = new ArrayList<>();
        finalTracks.addAll(selectedArtistTracks);
        finalTracks.addAll(genreAndSimilarTracks.stream()
            .limit(remainingTracks)
            .collect(Collectors.toList()));
        Collections.shuffle(finalTracks);
        
        // Ensure we have the target number of tracks
        if (finalTracks.size() > targetTrackCount) {
            finalTracks = finalTracks.subList(0, targetTrackCount);
        }
        
        Playlist playlist;
        if (!existingPlaylists.isEmpty() && needsRefresh) {
            playlist = existingPlaylists.get(0);
            playlist.setTracks(finalTracks);
            playlist.setUpdatedAt(LocalDateTime.now());
        } else {
            playlist = Playlist.builder()
                .name(playlistName)
                .type(PlaylistType.ARTIST_RADIO)
                .status(true)
                .cover(artist.getAvatar() != null ? artist.getAvatar() : 
                      "https://res.cloudinary.com/dll5rlqx9/image/upload/v1749306625/covers/gqpz9latmyqlkw68hu6s.jpg")
                .user(user)
                .tracks(finalTracks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        return playlistRepository.save(playlist);
    }
    
    @Override
    public void refreshArtistRadioPlaylists(Integer userId) {
        // Use the auto-generation method to handle refresh logic
        autoGenerateArtistRadioPlaylists(userId, 8);
        
        // Additionally, check and refresh any existing playlists that are older than 1 week
        List<Playlist> existingPlaylists = playlistRepository.findByTypeInAndUserId(
            Arrays.asList(PlaylistType.ARTIST_RADIO), userId);
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        
        for (Playlist playlist : existingPlaylists) {
            if (playlist.getUpdatedAt() == null || playlist.getUpdatedAt().isBefore(oneWeekAgo)) {
                // Extract artist name from playlist name and regenerate
                String artistName = playlist.getName().replace(" Radio", "");
                
                // Find the artist by name
                List<Artist> artists = artistRepository.findByNameContainingIgnoreCase(artistName);
                if (!artists.isEmpty()) {
                    Artist artist = artists.get(0);
                    try {
                        generateArtistRadio(userId, artist.getId());
                    } catch (Exception e) {
                        System.err.println("Error refreshing artist radio for " + artistName + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    @Override
    public void updateAllUserArtistRadios() {
        // Get all users who have favorite artists or recent listening history
        List<User> activeUsers = userRepository.findUsersWithFavoriteArtists();
        
        for (User user : activeUsers) {
            try {
                refreshArtistRadioPlaylists(user.getId());
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error updating artist radios for user " + user.getId() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Set playlist cover from first track or use default with text overlay
     */
    private void setPlaylistCoverFromFirstTrack(Playlist playlist) {
        if (playlist == null || playlist.getTracks() == null || playlist.getTracks().isEmpty()) {
            playlist.setCover(getDefaultCoverByType(playlist.getType()));
            return;
        }
        
        Track firstTrack = playlist.getTracks().get(0);
        String coverUrl = null;
        
        // Try to get cover from first track
        if (firstTrack.getCover() != null && !firstTrack.getCover().isEmpty()) {
            coverUrl = firstTrack.getCover();
        } 
        // Try album cover as fallback
        else if (firstTrack.getAlbum() != null && firstTrack.getAlbum().getCover() != null) {
            coverUrl = firstTrack.getAlbum().getCover();
        }
        
        // Use default if no cover found
        if (coverUrl == null || coverUrl.isEmpty()) {
            coverUrl = getDefaultCoverByType(playlist.getType());
        }
        
        playlist.setCover(coverUrl);
    }
    
    /**
     * Get default cover URL with text overlay based on playlist type
     */
    private String getDefaultCoverByType(PlaylistType type) {
        String baseUrl = "https://res.cloudinary.com/dll5rlqx9/image/upload/";
        
        switch (type) {
            default:
                return baseUrl + "v1749306625/covers/gqpz9latmyqlkw68hu6s.jpg";
        }
    }
    
    /**
     * Auto-generate artist radio playlists to ensure user always has the requested number
     */
    private void autoGenerateArtistRadioPlaylists(Integer userId, int targetCount) {
        
        // Get existing artist radio playlists
        List<Playlist> existingPlaylists = playlistRepository.findByTypeInAndUserId(
            Arrays.asList(PlaylistType.ARTIST_RADIO), userId);
        
        // Check which playlists need refresh (older than 1 week)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Playlist> freshPlaylists = existingPlaylists.stream()
            .filter(p -> p.getUpdatedAt() != null && p.getUpdatedAt().isAfter(oneWeekAgo))
            .collect(Collectors.toList());
        
        int playlistsNeeded = targetCount - freshPlaylists.size();
        
        if (playlistsNeeded <= 0) {
            return; // User already has enough fresh playlists
        }
        
        // Get user's favorite artists
        List<UserFavouriteArtist> favoriteArtists = userFavouriteArtistRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
        
        // Get artists from user's listening history
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Pageable historyPageable = PageRequest.of(0, 100);
        List<History> recentHistory = historyRepository.findUserHistorySince(userId, thirtyDaysAgo, historyPageable);
        
        Map<Artist, Long> artistPlayCounts = new HashMap<>();
        
        // Add favorite artists with higher weight
        for (UserFavouriteArtist favoriteArtist : favoriteArtists) {
            artistPlayCounts.put(favoriteArtist.getArtist(), 100L); // High weight for favorites
        }
        
        // Add artists from listening history
        for (History history : recentHistory) {
            if (history.getTrack() != null && history.getTrack().getArtists() != null) {
                for (Artist artist : history.getTrack().getArtists()) {
                    artistPlayCounts.merge(artist, 1L, Long::sum);
                }
            }
        }
        
        // If still not enough artists, get popular artists from user's interaction history
        if (artistPlayCounts.size() < playlistsNeeded) {
            Pageable interactionPageable = PageRequest.of(0, 50);
            List<UserTrackInteraction> userInteractions = userTrackInteractionRepository
                .findTopByUserIdOrderByInteractionScoreDesc(userId, interactionPageable);
            
            for (UserTrackInteraction interaction : userInteractions) {
                Track track = interaction.getTrack();
                if (track.getArtists() != null) {
                    for (Artist artist : track.getArtists()) {
                        artistPlayCounts.merge(artist, (long)(interaction.getInteractionScore() * 10), Long::sum);
                    }
                }
            }
        }
        
        // Get top artists and exclude those that already have fresh playlists
        Set<String> existingArtistNames = freshPlaylists.stream()
            .map(p -> p.getName().replace(" Radio", ""))
            .collect(Collectors.toSet());
        
        List<Artist> topArtists = artistPlayCounts.entrySet().stream()
            .filter(entry -> !existingArtistNames.contains(entry.getKey().getName()))
            .sorted(Map.Entry.<Artist, Long>comparingByValue().reversed())
            .limit(playlistsNeeded)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            topArtists.stream().map(Artist::getName).collect(Collectors.joining(", "));
        
        // Generate playlists for top artists
        int generatedCount = 0;
        for (Artist artist : topArtists) {
            try {
                Playlist generated = generateArtistRadio(userId, artist.getId());
                if (generated != null) {
                    generatedCount++;

                }
            } catch (Exception e) {
                System.err.println("Error generating artist radio for artist " + artist.getName() + " (ID: " + artist.getId() + "): " + e.getMessage());
            }
        }
        
    }
} 