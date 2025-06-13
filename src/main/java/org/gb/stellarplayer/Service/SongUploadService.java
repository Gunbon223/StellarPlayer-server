package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.*;
import org.gb.stellarplayer.Exception.DuplicateSongException;
import org.gb.stellarplayer.Repository.*;
import org.gb.stellarplayer.Request.UploadSongRequest;
import org.gb.stellarplayer.Response.SongUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Transactional
public class SongUploadService {
    
    private static final Logger log = LoggerFactory.getLogger(SongUploadService.class);
    
    private final CloudinaryService cloudinaryService;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    
    public SongUploadService(CloudinaryService cloudinaryService,
                           TrackRepository trackRepository,
                           ArtistRepository artistRepository,
                           AlbumRepository albumRepository,
                           GenreRepository genreRepository) {
        this.cloudinaryService = cloudinaryService;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
    }
    
    public Track uploadSong(MultipartFile audioFile, MultipartFile coverFile, UploadSongRequest request) throws IOException {
        log.info("Starting song upload process for: {}", request.getTitle());
        
        // Check if song already exists BEFORE uploading to Cloudinary (using artist names directly)
        List<String> lowercaseArtistNames = request.getArtistNames().stream()
            .map(name -> name.trim().toLowerCase())
            .collect(java.util.stream.Collectors.toList());
            
        log.info("Checking for duplicate with title: '{}' and artists: {}", request.getTitle().trim(), lowercaseArtistNames);
        
        // Upload cover image first (needed for potential track updates and new artists)
        String coverUrl = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            CloudinaryService.CloudinaryResponse coverResponse = cloudinaryService.uploadFile(coverFile, "covers");
            coverUrl = coverResponse.getUrl();
            log.info("Cover uploaded successfully: {}", coverUrl);
        }
        
        boolean trackExists = checkForDuplicates(request.getTitle().trim(), lowercaseArtistNames);
        
        if (trackExists) {
            log.info("Duplicate found, checking for new artists/genres to add...");
            // Use the available method to find tracks by artist names
            List<Track> matchingTracks = trackRepository.findByArtistNamesIn(lowercaseArtistNames);
            Track existingTrack = matchingTracks.stream()
                .filter(track -> track.getTitle().toLowerCase().equals(request.getTitle().trim().toLowerCase()))
                .findFirst()
                .orElse(null);
            
            if (existingTrack != null) {
                // Check for new artists and genres to add
                Track updatedTrack = updateTrackWithNewArtistsAndGenres(existingTrack, request, coverUrl);
                
                if (updatedTrack != null) {
                    log.info("Updated existing track '{}' with new artists/genres (ID: {})", 
                        updatedTrack.getTitle(), updatedTrack.getId());
                    return updatedTrack;
                } else {
                    // No new artists or genres, throw duplicate exception
                    String artistNames = existingTrack.getArtists().stream()
                        .map(Artist::getName)
                        .collect(java.util.stream.Collectors.joining(", "));
                    
                    log.warn("Duplicate track found with no new content: '{}' by {} (ID: {})", 
                        existingTrack.getTitle(), artistNames, existingTrack.getId());
                    
                    throw new DuplicateSongException(
                        existingTrack.getTitle(), 
                        artistNames, 
                        existingTrack.getId()
                    );
                }
            }
        }
        
        log.info("No duplicates found, proceeding with audio file upload...");
        
        // Upload audio file to Cloudinary
        CloudinaryService.CloudinaryResponse audioResponse = cloudinaryService.uploadAudio(audioFile, "tracks");
        log.info("Audio uploaded successfully: {}", audioResponse.getUrl());
        
        // Get or create artists
        List<Artist> artists = getOrCreateArtists(request.getArtistNames(), coverUrl);
        
        // Get or create album with release year
        Album album = getOrCreateAlbum(request.getAlbumTitle(), artists, coverUrl, request.getReleaseYear());
        
        // Get or create genres
        List<Genre> genres = getOrCreateGenres(request.getGenreNames());
        
        // Calculate duration from audio response (convert from seconds to seconds as integer)
        int duration = audioResponse.getDuration() != null ? audioResponse.getDuration().intValue() : 0;
        
        // Create and save track
        Track track = Track.builder()
                .title(request.getTitle())
                .duration(duration)
                .status(true)
                .path(audioResponse.getUrl())
                .cover(coverUrl)
                .releaseYear(request.getReleaseYear())
                .album(album)
                .artists(artists)
                .genres(genres)
                .playCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Track savedTrack = trackRepository.save(track);
        log.info("Track saved successfully with ID: {}", savedTrack.getId());
        
        return savedTrack;
    }
    
    private List<Artist> getOrCreateArtists(List<String> artistNames, String defaultAvatar) {
        List<Artist> artists = new ArrayList<>();
        
        for (String artistName : artistNames) {
            Artist artist = artistRepository.findByNameIgnoreCase(artistName.trim())
                    .orElseGet(() -> {
                        Artist newArtist = Artist.builder()
                                .name(artistName.trim())
                                .avatar(defaultAvatar)
                                .active(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        
                        Artist savedArtist = artistRepository.save(newArtist);
                        log.info("Created new artist: {} with ID: {}", artistName, savedArtist.getId());
                        return savedArtist;
                    });
            
            artists.add(artist);
        }
        
        return artists;
    }
    
    private Album getOrCreateAlbum(String albumTitle, List<Artist> artists, String defaultCover, Integer releaseYear) {
        if (albumTitle == null || albumTitle.trim().isEmpty()) {
            return null; // No album specified
        }
        
        return albumRepository.findByTitleIgnoreCaseAndArtistsIn(albumTitle.trim(), artists)
                .orElseGet(() -> {
                    // Convert release year to LocalDate (January 1st of that year)
                    LocalDate releaseDate = releaseYear != null ? 
                        LocalDate.of(releaseYear, 1, 1) : null;
                    
                    Album newAlbum = Album.builder()
                            .title(albumTitle.trim())
                            .cover(defaultCover)
                            .status(true)
                            .releaseDate(releaseDate) // Set release date from song's release year
                            .artists(artists)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    Album savedAlbum = albumRepository.save(newAlbum);
                    log.info("Created new album: {} with ID: {} and release year: {}", albumTitle, savedAlbum.getId(), releaseYear);
                    return savedAlbum;
                });
    }
    
    private List<Genre> getOrCreateGenres(List<String> genreNames) {
        List<Genre> genres = new ArrayList<>();
        
        if (genreNames == null || genreNames.isEmpty()) {
            return genres;
        }
        
        for (String genreName : genreNames) {
            try {
                log.info("Processing genre: {}", genreName.trim());
                
                // Use findFirst to handle potential duplicates
                List<Genre> existingGenres = genreRepository.findByNameContainingIgnoreCase(genreName.trim());
                Genre genre = existingGenres.stream()
                    .filter(g -> g.getName().equalsIgnoreCase(genreName.trim()))
                    .findFirst()
                    .orElse(null);
                
                if (genre != null) {
                    log.info("Found existing genre: {} with ID: {}", genre.getName(), genre.getId());
                    genres.add(genre);
                } else {
                    // Create new genre if not exists
                    try {
                        Genre newGenre = Genre.builder()
                                .name(genreName.trim())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        
                        Genre savedGenre = genreRepository.save(newGenre);
                        log.info("Created new genre: {} with ID: {}", genreName.trim(), savedGenre.getId());
                        genres.add(savedGenre);
                    } catch (Exception saveException) {
                        log.warn("Failed to create genre '{}', trying to find existing one: {}", genreName.trim(), saveException.getMessage());
                        
                        // If save failed (maybe due to constraint), try to find it again
                        List<Genre> retryGenres = genreRepository.findByNameContainingIgnoreCase(genreName.trim());
                        Genre retryGenre = retryGenres.stream()
                            .filter(g -> g.getName().equalsIgnoreCase(genreName.trim()))
                            .findFirst()
                            .orElse(null);
                        
                        if (retryGenre != null) {
                            log.info("Found genre on retry: {} with ID: {}", retryGenre.getName(), retryGenre.getId());
                            genres.add(retryGenre);
                        } else {
                            log.error("Could not create or find genre: {}", genreName.trim());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing genre '{}': {}", genreName.trim(), e.getMessage());
                // Skip this genre and continue with others
                continue;
            }
        }
        
        return genres;
    }
    
    private List<Artist> getExistingArtists(List<String> artistNames) {
        List<Artist> existingArtists = new ArrayList<>();
        
        for (String artistName : artistNames) {
            artistRepository.findByNameIgnoreCase(artistName.trim())
                .ifPresent(existingArtists::add);
        }
        
        return existingArtists;
    }
    
    public SongUploadResponse createSongUploadResponse(Track track) {
        return SongUploadResponse.builder()
                .success(true)
                .message("Song uploaded successfully")
                .track(SongUploadResponse.TrackInfo.builder()
                        .id(track.getId())
                        .title(track.getTitle())
                        .duration(track.getDuration())
                        .path(track.getPath())
                        .cover(track.getCover())
                        .releaseYear(track.getReleaseYear())
                        .album(track.getAlbum() != null ? SongUploadResponse.AlbumInfo.builder()
                                .id(track.getAlbum().getId())
                                .title(track.getAlbum().getTitle())
                                .cover(track.getAlbum().getCover())
                                .build() : null)
                        .artists(track.getArtists().stream()
                                .map(artist -> SongUploadResponse.ArtistInfo.builder()
                                        .id(artist.getId())
                                        .name(artist.getName())
                                        .avatar(artist.getAvatar())
                                        .build())
                                .collect(java.util.stream.Collectors.toList()))
                        .genres(track.getGenres() != null ? track.getGenres().stream()
                                .map(genre -> SongUploadResponse.GenreInfo.builder()
                                        .id(genre.getId())
                                        .name(genre.getName())
                                        .build())
                                .collect(java.util.stream.Collectors.toList()) : new ArrayList<>())
                        .createdAt(track.getCreatedAt())
                        .build())
                .build();
    }
    
    public Track uploadSongOptimized(MultipartFile audioFile, MultipartFile coverFile, UploadSongRequest request) throws IOException {
        log.info("Starting optimized song upload process for: {}", request.getTitle());
        
        // Fast duplicate check using safe method
        List<String> lowercaseArtistNames = request.getArtistNames().stream()
            .map(name -> name.toLowerCase())
            .collect(Collectors.toList());
        boolean trackExists = checkForDuplicates(request.getTitle(), lowercaseArtistNames);
        
        if (trackExists) {
            log.info("Duplicate found in optimized upload, checking for new artists/genres to add...");
            // Use the available method to find tracks by artist names
            List<Track> matchingTracks = trackRepository.findByArtistNamesIn(lowercaseArtistNames);
            Track existingTrack = matchingTracks.stream()
                .filter(track -> track.getTitle().toLowerCase().equals(request.getTitle().toLowerCase()))
                .findFirst()
                .orElse(null);
            
            if (existingTrack != null) {
                // For optimized version, get cover URL first
                String coverUrl = null;
                if (coverFile != null && !coverFile.isEmpty()) {
                    try {
                        CloudinaryService.CloudinaryResponse coverResponse = cloudinaryService.uploadFile(coverFile, "covers");
                        coverUrl = coverResponse.getUrl();
                        log.info("Cover uploaded for track update: {}", coverUrl);
                    } catch (IOException e) {
                        log.warn("Cover upload failed during track update, continuing without cover: " + e.getMessage());
                    }
                }
                
                // Check for new artists and genres to add
                Track updatedTrack = updateTrackWithNewArtistsAndGenres(existingTrack, request, coverUrl);
                
                if (updatedTrack != null) {
                    log.info("Updated existing track '{}' with new artists/genres in optimized mode (ID: {})", 
                        updatedTrack.getTitle(), updatedTrack.getId());
                    return updatedTrack;
                } else {
                    // No new artists or genres, throw duplicate exception
                    String artistNames = existingTrack.getArtists().stream()
                        .map(Artist::getName)
                        .collect(Collectors.joining(", "));
                    
                    throw new DuplicateSongException(
                        existingTrack.getTitle(), 
                        artistNames, 
                        existingTrack.getId()
                    );
                }
            }
        }
        
        log.info("No duplicates found, proceeding with optimized upload...");
        
        try {
            // Upload files in parallel using CompletableFuture
            CompletableFuture<CloudinaryService.CloudinaryResponse> audioUploadFuture = 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return cloudinaryService.uploadAudio(audioFile, "tracks");
                    } catch (IOException e) {
                        throw new RuntimeException("Audio upload failed: " + e.getMessage(), e);
                    }
                });
            
            CompletableFuture<String> coverUploadFuture = 
                CompletableFuture.supplyAsync(() -> {
                    if (coverFile != null && !coverFile.isEmpty()) {
                        try {
                            CloudinaryService.CloudinaryResponse coverResponse = 
                                cloudinaryService.uploadFile(coverFile, "covers");
                            return coverResponse.getUrl();
                        } catch (IOException e) {
                            log.warn("Cover upload failed, continuing without cover: " + e.getMessage());
                            return null;
                        }
                    }
                    return null;
                });
            
            // Wait for cover upload first since we need it for artist avatars
            String coverUrl = coverUploadFuture.get();
            
            // Process artists and genres in parallel while audio uploads
            CompletableFuture<List<Artist>> artistsFuture = 
                CompletableFuture.supplyAsync(() -> getOrCreateArtistsBatch(request.getArtistNames(), coverUrl));
            
            CompletableFuture<List<Genre>> genresFuture = 
                CompletableFuture.supplyAsync(() -> getOrCreateGenresBatch(request.getGenreNames()));
            
            // Wait for all parallel operations to complete
            CloudinaryService.CloudinaryResponse audioResponse = audioUploadFuture.get();
            List<Artist> artists = artistsFuture.get();
            List<Genre> genres = genresFuture.get();
            
            log.info("All parallel operations completed successfully");
            
            // Get or create album (depends on artists, so can't be fully parallel)
            Album album = getOrCreateAlbumOptimized(request.getAlbumTitle(), artists, coverUrl, request.getReleaseYear());
            
            // Calculate duration
            int duration = audioResponse.getDuration() != null ? audioResponse.getDuration().intValue() : 0;
            
            // Create and save track
            Track track = Track.builder()
                    .title(request.getTitle())
                    .duration(duration)
                    .status(true)
                    .path(audioResponse.getUrl())
                    .cover(coverUrl)
                    .releaseYear(request.getReleaseYear())
                    .album(album)
                    .artists(artists)
                    .genres(genres)
                    .playCount(0L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            Track savedTrack = trackRepository.save(track);
            log.info("Track saved successfully with ID: {}", savedTrack.getId());
            
            return savedTrack;
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error in parallel processing: {}", e.getMessage());
            throw new IOException("Upload processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Optimized batch processing for artists
     */
    private List<Artist> getOrCreateArtistsBatch(List<String> artistNames, String defaultAvatar) {
        // First, try to find all existing artists in a single query
        Map<String, Artist> existingArtists = artistRepository.findAll().stream()
            .filter(artist -> artistNames.stream()
                .anyMatch(name -> name.equalsIgnoreCase(artist.getName())))
            .collect(Collectors.toMap(
                artist -> artist.getName().toLowerCase(),
                artist -> artist,
                (existing, replacement) -> existing // Keep first in case of duplicates
            ));
        
        List<Artist> result = new ArrayList<>();
        List<Artist> newArtists = new ArrayList<>();
        
        for (String artistName : artistNames) {
            String lowerName = artistName.toLowerCase();
            Artist existingArtist = existingArtists.get(lowerName);
            
            if (existingArtist != null) {
                result.add(existingArtist);
            } else {
                // Create new artist with track cover as avatar
                Artist newArtist = Artist.builder()
                        .name(artistName.trim())
                        .avatar(defaultAvatar) // Use track cover as avatar for new artists
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                newArtists.add(newArtist);
            }
        }
        
        // Batch save new artists
        if (!newArtists.isEmpty()) {
            List<Artist> savedArtists = artistRepository.saveAll(newArtists);
            result.addAll(savedArtists);
            log.info("Batch created {} new artists with cover as avatar", savedArtists.size());
        }
        
        return result;
    }
    
    /**
     * Optimized batch processing for genres
     */
    private List<Genre> getOrCreateGenresBatch(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Find existing genres by exact name match (case-insensitive)
        Map<String, Genre> existingGenres = genreRepository.findAll().stream()
            .filter(genre -> genreNames.stream()
                .anyMatch(name -> name.equalsIgnoreCase(genre.getName())))
            .collect(Collectors.toMap(
                genre -> genre.getName().toLowerCase(),
                genre -> genre,
                (existing, replacement) -> existing
            ));
        
        List<Genre> result = new ArrayList<>();
        List<Genre> newGenres = new ArrayList<>();
        
        for (String genreName : genreNames) {
            String lowerName = genreName.toLowerCase();
            Genre existingGenre = existingGenres.get(lowerName);
            
            if (existingGenre != null) {
                result.add(existingGenre);
            } else {
                Genre newGenre = Genre.builder()
                        .name(genreName.trim())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                newGenres.add(newGenre);
            }
        }
        
        // Batch save new genres
        if (!newGenres.isEmpty()) {
            try {
                List<Genre> savedGenres = genreRepository.saveAll(newGenres);
                result.addAll(savedGenres);
                log.info("Batch created {} new genres", savedGenres.size());
            } catch (Exception e) {
                log.warn("Batch genre creation failed, falling back to individual saves: {}", e.getMessage());
                // Fallback to individual saves in case of constraint violations
                for (Genre newGenre : newGenres) {
                    try {
                        Genre saved = genreRepository.save(newGenre);
                        result.add(saved);
                    } catch (Exception saveError) {
                        log.warn("Failed to save genre '{}': {}", newGenre.getName(), saveError.getMessage());
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Optimized album creation/retrieval
     */
    private Album getOrCreateAlbumOptimized(String albumTitle, List<Artist> artists, String defaultCover, Integer releaseYear) {
        if (albumTitle == null || albumTitle.trim().isEmpty()) {
            return null;
        }
        
        return albumRepository.findByTitleIgnoreCaseAndArtistsIn(albumTitle.trim(), artists)
                .orElseGet(() -> {
                    // Convert release year to LocalDate (January 1st of that year)
                    LocalDate releaseDate = releaseYear != null ? 
                        LocalDate.of(releaseYear, 1, 1) : null;
                    
                    Album newAlbum = Album.builder()
                            .title(albumTitle.trim())
                            .cover(defaultCover)
                            .status(true)
                            .releaseDate(releaseDate) // Set release date from song's release year
                            .artists(artists)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    Album savedAlbum = albumRepository.save(newAlbum);
                    log.info("Created new album: {} with ID: {} and release year: {}", albumTitle, savedAlbum.getId(), releaseYear);
                    return savedAlbum;
                });
    }
    
    /**
     * Optimized response creation with minimal object creation
     */
    public SongUploadResponse createSongUploadResponseOptimized(Track track) {
        // Pre-calculate collections to avoid multiple iterations
        List<SongUploadResponse.ArtistInfo> artistInfos = track.getArtists().stream()
                .map(artist -> SongUploadResponse.ArtistInfo.builder()
                        .id(artist.getId())
                        .name(artist.getName())
                        .avatar(artist.getAvatar())
                        .build())
                .collect(Collectors.toList());
        
        List<SongUploadResponse.GenreInfo> genreInfos = track.getGenres() != null 
            ? track.getGenres().stream()
                .map(genre -> SongUploadResponse.GenreInfo.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .build())
                .collect(Collectors.toList())
            : new ArrayList<>();
        
        SongUploadResponse.AlbumInfo albumInfo = null;
        if (track.getAlbum() != null) {
            albumInfo = SongUploadResponse.AlbumInfo.builder()
                    .id(track.getAlbum().getId())
                    .title(track.getAlbum().getTitle())
                    .cover(track.getAlbum().getCover())
                    .build();
        }
        
        return SongUploadResponse.builder()
                .success(true)
                .message("Song uploaded successfully")
                .track(SongUploadResponse.TrackInfo.builder()
                        .id(track.getId())
                        .title(track.getTitle())
                        .duration(track.getDuration())
                        .path(track.getPath())
                        .cover(track.getCover())
                        .releaseYear(track.getReleaseYear())
                        .album(albumInfo)
                        .artists(artistInfos)
                        .genres(genreInfos)
                        .createdAt(track.getCreatedAt())
                        .build())
                .build();
    }
    
    /**
     * Helper method to safely check for duplicates without casting issues
     */
    private boolean checkForDuplicates(String title, List<String> artistNames) {
        try {
            // Try the optimized query first
            return trackRepository.existsByTitleAndArtistNamesOptimized(title, artistNames);
        } catch (ClassCastException e) {
            log.warn("Database casting issue with optimized query, falling back to safe method: {}", e.getMessage());
            // Fallback to a safer approach
            List<Track> matchingTracks = trackRepository.findByArtistNamesIn(artistNames);
            return matchingTracks.stream()
                .anyMatch(track -> track.getTitle().toLowerCase().equals(title.toLowerCase()));
        }
    }
    
    /**
     * Update existing track with new artists and genres if any
     * @param existingTrack The existing track to update
     * @param request The upload request with potentially new artists/genres
     * @param coverUrl The cover URL to use for new artists
     * @return Updated track if changes were made, null if no updates needed
     */
    private Track updateTrackWithNewArtistsAndGenres(Track existingTrack, UploadSongRequest request, String coverUrl) {
        boolean hasUpdates = false;
        
        // Check for new artists
        List<String> existingArtistNames = existingTrack.getArtists().stream()
            .map(artist -> artist.getName().toLowerCase())
            .collect(Collectors.toList());
        
        List<String> newArtistNames = request.getArtistNames().stream()
            .map(name -> name.toLowerCase())
            .filter(name -> !existingArtistNames.contains(name))
            .collect(Collectors.toList());
        
        if (!newArtistNames.isEmpty()) {
            log.info("Found {} new artists to add: {}", newArtistNames.size(), newArtistNames);
            
            // Get or create new artists
            List<Artist> newArtists = getOrCreateArtists(
                newArtistNames.stream()
                    .map(name -> request.getArtistNames().stream()
                        .filter(original -> original.toLowerCase().equals(name))
                        .findFirst()
                        .orElse(name))
                    .collect(Collectors.toList()), 
                coverUrl
            );
            
            // Add new artists to existing track
            List<Artist> allArtists = new ArrayList<>(existingTrack.getArtists());
            allArtists.addAll(newArtists);
            existingTrack.setArtists(allArtists);
            hasUpdates = true;
        }
        
        // Check for new genres
        if (request.getGenreNames() != null && !request.getGenreNames().isEmpty()) {
            List<String> existingGenreNames = existingTrack.getGenres().stream()
                .map(genre -> genre.getName().toLowerCase())
                .collect(Collectors.toList());
            
            List<String> newGenreNames = request.getGenreNames().stream()
                .map(name -> name.toLowerCase())
                .filter(name -> !existingGenreNames.contains(name))
                .collect(Collectors.toList());
            
            if (!newGenreNames.isEmpty()) {
                log.info("Found {} new genres to add: {}", newGenreNames.size(), newGenreNames);
                
                // Get or create new genres
                List<Genre> newGenres = getOrCreateGenres(
                    newGenreNames.stream()
                        .map(name -> request.getGenreNames().stream()
                            .filter(original -> original.toLowerCase().equals(name))
                            .findFirst()
                            .orElse(name))
                        .collect(Collectors.toList())
                );
                
                // Add new genres to existing track
                List<Genre> allGenres = new ArrayList<>(existingTrack.getGenres());
                allGenres.addAll(newGenres);
                existingTrack.setGenres(allGenres);
                hasUpdates = true;
            }
        }
        
        if (hasUpdates) {
            // Update timestamp
            existingTrack.setUpdatedAt(LocalDateTime.now());
            
            // Save and return updated track
            Track updatedTrack = trackRepository.save(existingTrack);
            log.info("Successfully updated track '{}' with new artists/genres", updatedTrack.getTitle());
            return updatedTrack;
        }
        
        return null; // No updates needed
    }
} 