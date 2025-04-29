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
@Table(name = "favourite")
public class Favourite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    User user;
    @ManyToOne
    Playlist playlist;
    @ManyToOne
    Album album;
    @ManyToOne
    Artist artist;
    @ManyToOne
    Track track;


    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
