package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser(User user);
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
