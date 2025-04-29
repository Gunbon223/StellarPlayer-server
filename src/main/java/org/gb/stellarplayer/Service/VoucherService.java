package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.Voucher;

import java.util.List;

public interface VoucherService {
    List<Voucher> getAllVouchers();
    Voucher getVoucherById(int id);
    Voucher getVoucherByCode(String code);
    Voucher createVoucher(Voucher voucher);
    Voucher updateVoucher(Voucher voucher);
    void deleteVoucher(int id);
    
    // Voucher validation and application
    boolean isVoucherValid(String code);
    boolean isVoucherValidForUser(String code, User user);
    Voucher applyVoucher(String code, User user);
    void incrementVoucherUsage(Voucher voucher);
}
