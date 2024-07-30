package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Role;
import org.gb.stellarplayer.Model.Enum.EnumUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(EnumUserRole name);
}
