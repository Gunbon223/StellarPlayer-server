package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreSearchDTO {
    private Integer id;
    private String name;
    private String cover_path;
}
