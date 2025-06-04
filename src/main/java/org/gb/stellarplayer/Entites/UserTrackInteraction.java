package org.gb.stellarplayer.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_track_interaction",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "track_id"}))
public class UserTrackInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;
    
    // Interaction score based on user behavior (0.0 - 5.0)
    // Calculated from plays, likes, skips, time listened, etc.
    @Column(name = "interaction_score", columnDefinition = "DECIMAL(3,2) DEFAULT 0.0")
    private Double interactionScore = 0.0;
    
    @Column(name = "play_count", columnDefinition = "INT DEFAULT 0")
    private Integer playCount = 0;
    
    @Column(name = "total_listen_time", columnDefinition = "BIGINT DEFAULT 0")
    private Long totalListenTime = 0L; // in seconds
    
    @Column(name = "skip_count", columnDefinition = "INT DEFAULT 0")
    private Integer skipCount = 0;
    
    @Column(name = "is_liked", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLiked = false;
    
    @Column(name = "is_shared", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isShared = false;
    
    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastInteractionAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastInteractionAt = LocalDateTime.now();
    }
    
    /**
     * Calculate interaction score based on user behavior
     * Formula: (playCount * 1.0) + (listenTimeRatio * 2.0) + (isLiked ? 1.5 : 0) 
     *         + (isShared ? 1.0 : 0) - (skipCount * 0.5)
     */
    public void calculateInteractionScore(Integer trackDuration) {
        if (trackDuration == null || trackDuration == 0) {
            trackDuration = 180; // Default 3 minutes
        }
        
        double playScore = Math.min(playCount * 1.0, 3.0);
        double listenTimeRatio = (double) totalListenTime / (trackDuration * playCount);
        double listenScore = Math.min(listenTimeRatio * 2.0, 2.0);
        double likeScore = isLiked ? 1.5 : 0;
        double shareScore = isShared ? 1.0 : 0;
        double skipPenalty = Math.min(skipCount * 0.5, 2.0);
        
        this.interactionScore = Math.max(0.0, Math.min(5.0, 
            playScore + listenScore + likeScore + shareScore - skipPenalty));
    }
} 