package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Album;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    Optional<UserSubscription> findByUser(User user);

}
