package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderDetails, Integer> {
}
