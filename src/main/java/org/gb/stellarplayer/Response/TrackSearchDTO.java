package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackSearchDTO {
    private Integer id;
    private String title;
    private int duration;
    private String cover;
    private String albumTitle;
    private List<String> artistNames;
}
