package org.gb.stellarplayer.Controller;

import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Exception.DuplicateSongException;
import org.gb.stellarplayer.Request.UploadSongRequest;
import org.gb.stellarplayer.Response.SongUploadResponse;
import org.gb.stellarplayer.Service.CloudinaryService;
import org.gb.stellarplayer.Service.SongUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/files")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    private final CloudinaryService cloudinaryService;
    private final SongUploadService songUploadService;

    public FileUploadController(CloudinaryService cloudinaryService, SongUploadService songUploadService) {
        this.cloudinaryService = cloudinaryService;
        this.songUploadService = songUploadService;
    }

    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("folder") String folderName) {
        try {
            log.info("Received image upload request - File: {}, Folder: {}", file.getOriginalFilename(), folderName);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }
            
            CloudinaryService.CloudinaryResponse response = cloudinaryService.uploadFile(file, folderName);
            log.info("Image uploaded successfully: {}", response.getUrl());
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload image: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unexpected error occurred"));
        }
    }

    @PostMapping("/upload/video")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file,
                                         @RequestParam("folder") String folderName) {
        try {
            log.info("Received video upload request - File: {}, Folder: {}", file.getOriginalFilename(), folderName);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }
            
            CloudinaryService.CloudinaryResponse response = cloudinaryService.uploadVideo(file, folderName);
            log.info("Video uploaded successfully: {}", response.getUrl());
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading video: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload video: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading video: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unexpected error occurred"));
        }
    }

    @PostMapping("/upload/audio")
    public ResponseEntity<?> uploadAudio(@RequestParam("file") MultipartFile file,
                                         @RequestParam("folder") String folderName) {
        try {
            log.info("Received audio upload request - File: {}, Folder: {}", file.getOriginalFilename(), folderName);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }
            
            CloudinaryService.CloudinaryResponse response = cloudinaryService.uploadAudio(file, folderName);
            log.info("Audio uploaded successfully: {}", response.getUrl());
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading audio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload audio: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading audio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unexpected error occurred"));
        }
    }

    @PostMapping("/upload/song")
    public ResponseEntity<?> uploadSong(@RequestParam("audioFile") MultipartFile audioFile,
                                       @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                                       @RequestParam("title") String title,
                                       @RequestParam("releaseYear") Integer releaseYear,
                                       @RequestParam("artistNames") String artistNames,
                                       @RequestParam(value = "albumTitle", required = false) String albumTitle,
                                       @RequestParam(value = "genreNames", required = false) String genreNames) {
        try {
            log.info("Received song upload request - File: {}, Title: {}", audioFile.getOriginalFilename(), title);
            
            // Fast validation checks first
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Audio file is required"));
            }
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Title is required"));
            }
            
            if (artistNames == null || artistNames.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Artist names are required"));
            }
            
            // Optimized string parsing with pre-allocation
            List<String> artistNamesList = parseNames(artistNames);
            if (artistNamesList.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("At least one valid artist name is required"));
            }
            
            List<String> genreNamesList = genreNames != null && !genreNames.trim().isEmpty() 
                ? parseNames(genreNames) : null;
            
            // Create request object
            UploadSongRequest request = new UploadSongRequest();
            request.setTitle(title.trim());
            request.setReleaseYear(releaseYear);
            request.setArtistNames(artistNamesList);
            request.setAlbumTitle(albumTitle != null ? albumTitle.trim() : null);
            request.setGenreNames(genreNamesList);
            
            // Upload song using optimized service
            Track savedTrack = songUploadService.uploadSongOptimized(audioFile, coverFile, request);
            
            log.info("Song uploaded successfully with ID: {}", savedTrack.getId());
            
            // Create response efficiently
            SongUploadResponse response = songUploadService.createSongUploadResponseOptimized(savedTrack);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading song: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload song: " + e.getMessage()));
        } catch (DuplicateSongException e) {
            log.warn("Duplicate song upload attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createDuplicateErrorResponse(e));
        } catch (RuntimeException e) {
            log.error("Runtime error uploading song: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload song: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading song: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unexpected error occurred"));
        }
    }

    /**
     * Optimized string parsing for artist/genre names
     * Pre-allocates ArrayList and uses more efficient splitting
     */
    private List<String> parseNames(String names) {
        if (names == null || names.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Pre-allocate with estimated capacity
        String[] parts = names.split("[,/]");
        List<String> result = new ArrayList<>(parts.length);
        
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        
        return result;
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", "error");
        return errorResponse;
    }

    private Map<String, Object> createDuplicateErrorResponse(DuplicateSongException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        errorResponse.put("status", "duplicate");
        errorResponse.put("existingTrackId", e.getExistingTrackId());
        errorResponse.put("songTitle", e.getSongTitle());
        errorResponse.put("artistNames", e.getArtistNames());
        return errorResponse;
    }
}