package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PaymentTransaction entity
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    List<PaymentTransaction> findByOrderOrderByTransactionTimeDesc(Order order);
    List<PaymentTransaction> findByTransactionId(String transactionId);
}
