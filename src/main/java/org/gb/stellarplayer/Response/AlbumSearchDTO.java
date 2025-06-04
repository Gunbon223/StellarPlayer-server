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
public class AlbumSearchDTO {
    private Integer id;
    private String title;
    private String cover;
    private List<String> artistNames;
    private String releaseDate;
}
