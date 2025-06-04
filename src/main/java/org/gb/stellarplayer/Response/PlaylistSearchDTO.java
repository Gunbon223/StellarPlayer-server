package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSearchDTO {
    private Integer id;
    private String name;
    private String description;
    private String cover;
    private String creatorName;
    private int trackCount;
}
