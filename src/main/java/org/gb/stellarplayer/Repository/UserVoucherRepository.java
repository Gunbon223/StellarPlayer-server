package org.gb.stellarplayer.Repository;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserVoucher;
import org.gb.stellarplayer.Entites.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    List<UserVoucher> findByUser(User user);
    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);
    boolean existsByUserAndVoucher(User user, Voucher voucher);
}
