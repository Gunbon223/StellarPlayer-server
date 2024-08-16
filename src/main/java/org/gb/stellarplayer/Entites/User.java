package org.gb.stellarplayer.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Model.Enum.EnumUserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false)
    String name;
    @Column(nullable = false, unique = true)
    String email;
    String password;
    String avatar;
    LocalDate dob;

    @ManyToMany(fetch = FetchType.EAGER)
            @JoinTable(name = "user_role",
                    joinColumns = @JoinColumn(name = "user_id"),
                    inverseJoinColumns = @JoinColumn(name = "role_id"))
            private List<Role> roles;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<Favorite> favorites;



}
