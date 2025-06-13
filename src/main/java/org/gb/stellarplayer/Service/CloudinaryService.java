package org.gb.stellarplayer.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CloudinaryService {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public CloudinaryResponse uploadFile(MultipartFile file, String folderName) throws IOException {
        try {
            log.info("Uploading image file: {} to folder: {}", file.getOriginalFilename(), folderName);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folderName,
                            "resource_type", "image"
                    ));
            
            return buildResponse(uploadResult, "image");
        } catch (IOException e) {
            log.error("Error uploading image file: {}", e.getMessage());
            throw new IOException("Failed to upload image: " + e.getMessage());
        }
    }

    public CloudinaryResponse uploadVideo(MultipartFile file, String folderName) throws IOException {
        try {
            log.info("Uploading video file: {} to folder: {}", file.getOriginalFilename(), folderName);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", folderName,
                            "chunk_size", 6000000, // 6MB chunks for better upload performance
                            "eager", ObjectUtils.asMap(
                                    "quality", "auto",
                                    "fetch_format", "auto"
                            )
                    ));
            
            return buildResponse(uploadResult, "video");
        } catch (IOException e) {
            log.error("Error uploading video file: {}", e.getMessage());
            throw new IOException("Failed to upload video: " + e.getMessage());
        }
    }

    public CloudinaryResponse uploadAudio(MultipartFile file, String folderName) throws IOException {
        try {
            log.info("Uploading audio file: {} to folder: {}", file.getOriginalFilename(), folderName);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video", // Cloudinary treats audio as video resource type
                            "folder", folderName,
                            "format", "mp3", // Optimize for web playback
                            "quality", "auto",
                            "bit_rate", "128k" // Optimize audio bitrate for web streaming
                    ));
            
            return buildResponse(uploadResult, "audio");
        } catch (IOException e) {
            log.error("Error uploading audio file: {}", e.getMessage());
            throw new IOException("Failed to upload audio: " + e.getMessage());
        }
    }

    /**
     * Async audio upload for parallel processing
     */
    public CompletableFuture<CloudinaryResponse> uploadAudioAsync(MultipartFile file, String folderName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return uploadAudio(file, folderName);
            } catch (IOException e) {
                throw new RuntimeException("Async audio upload failed: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Async image upload for parallel processing
     */
    public CompletableFuture<CloudinaryResponse> uploadFileAsync(MultipartFile file, String folderName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return uploadFile(file, folderName);
            } catch (IOException e) {
                throw new RuntimeException("Async file upload failed: " + e.getMessage(), e);
            }
        });
    }

    private CloudinaryResponse buildResponse(Map<String, Object> uploadResult, String resourceType) {
        CloudinaryResponse response = new CloudinaryResponse();
        response.setUrl((String) uploadResult.get("secure_url"));
        response.setPublicId((String) uploadResult.get("public_id"));
        response.setResourceType(resourceType);
        response.setFormat((String) uploadResult.get("format"));
        response.setVersion(uploadResult.get("version").toString());
        response.setWidth(uploadResult.get("width"));
        response.setHeight(uploadResult.get("height"));
        response.setBytes((Integer) uploadResult.get("bytes"));
        response.setCreatedAt((String) uploadResult.get("created_at"));
        
        // Add duration for video/audio files
        if (uploadResult.containsKey("duration")) {
            response.setDuration((Double) uploadResult.get("duration"));
        }
        
        log.info("Successfully uploaded {} with public_id: {}", resourceType, response.getPublicId());
        return response;
    }

    // Response DTO class
    public static class CloudinaryResponse {
        private String url;
        private String publicId;
        private String resourceType;
        private String format;
        private String version;
        private Object width;
        private Object height;
        private Integer bytes;
        private String createdAt;
        private Double duration;

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPublicId() {
            return publicId;
        }

        public void setPublicId(String publicId) {
            this.publicId = publicId;
        }

        public String getResourceType() {
            return resourceType;
        }

        public void setResourceType(String resourceType) {
            this.resourceType = resourceType;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Object getWidth() {
            return width;
        }

        public void setWidth(Object width) {
            this.width = width;
        }

        public Object getHeight() {
            return height;
        }

        public void setHeight(Object height) {
            this.height = height;
        }

        public Integer getBytes() {
            return bytes;
        }

        public void setBytes(Integer bytes) {
            this.bytes = bytes;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public Double getDuration() {
            return duration;
        }

        public void setDuration(Double duration) {
            this.duration = duration;
        }
    }
}
