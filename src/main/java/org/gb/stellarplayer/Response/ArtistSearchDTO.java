package org.gb.stellarplayer.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistSearchDTO {
    private Integer id;
    private String name;
    private String avatar;
    private String bio;
    Boolean active;

} 