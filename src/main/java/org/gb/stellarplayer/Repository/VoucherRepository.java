package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.code = ?1 AND v.startDate <= ?2 AND v.endDate >= ?2 AND v.usedCount < v.maxUseCount")
    Optional<Voucher> findValidVoucherByCode(String code, LocalDateTime currentTime);

    @Query("SELECT v FROM Voucher v WHERE v.endDate > ?1 AND v.usedCount < v.maxUseCount")
    List<Voucher> findActiveVouchers(LocalDateTime currentTime);
}
