package org.gb.stellarplayer.Entites;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer orderNumber;

    int playedAt;

    LocalDateTime updatedAt;

    @OneToOne
    User user;

    @ManyToOne
    Playlist playlist;
    @ManyToOne
    Album album;
    @ManyToOne
    Track track;



}
