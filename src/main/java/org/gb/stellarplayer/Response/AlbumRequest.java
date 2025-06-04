package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequest {
    private String title;
    private String cover;
    private boolean status;
    private List<Integer> artists;
    private LocalDate releaseDate;private String genre;
    private Integer artist;
}
