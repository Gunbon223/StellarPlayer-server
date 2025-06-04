package org.gb.stellarplayer.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Model.Enum.DateType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "track")
public class Track {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;
    private String title;
    int duration;
    boolean status;
    String path;
    String cover;
    @Column(columnDefinition = "TEXT")
    String lyrics;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "play_count", columnDefinition = "bigint default 0")
    private Long playCount = 0L;

    @Column(name = "last_played_at")
    private LocalDateTime lastPlayedAt;

    @Column(name = "min_listen_duration")
    private Integer minListenDuration = 30; // Minimum seconds to count as a play

    @Column(name = "fraud_threshold")
    private Integer fraudThreshold = 100; // Maximum plays allowed per day

    @Column(name = "daily_play_count")
    private Integer dailyPlayCount = 0;

    @Column(name = "last_daily_reset")
    private LocalDateTime lastDailyReset;

    @Column(name = "is_suspicious")
    private Boolean isSuspicious = false;

    @Column(name = "suspicious_reason")
    private String suspiciousReason;

    @Column(name = "likes", columnDefinition = "bigint default 0")
    private Long likes = 0L;

    @Column(name = "shares", columnDefinition = "bigint default 0")
    private Long shares = 0L;

    @Column(name = "comments", columnDefinition = "bigint default 0")
    private Long comments = 0L;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany
    @JoinTable(
            name = "track_artist",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @Builder.Default
    private List<Artist> artists = new ArrayList<>();

    //ADD GENRE
    @ManyToMany
    @JoinTable(
            name = "track_genre",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    List<Genre> genres;



}
