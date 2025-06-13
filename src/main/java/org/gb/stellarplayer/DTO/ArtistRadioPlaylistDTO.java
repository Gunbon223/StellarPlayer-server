package org.gb.stellarplayer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistRadioPlaylistDTO {
    private Integer id;
    private String name;
    private String cover;
    private int trackCount;
    private String artistName;
    private LocalDateTime lastUpdated;
    private boolean needsRefresh;
    private Integer artistId;
} 