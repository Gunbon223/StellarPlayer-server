package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.UserVoucher;
import org.gb.stellarplayer.Entites.Voucher;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserVoucherRepository;
import org.gb.stellarplayer.Repository.VoucherRepository;
import org.gb.stellarplayer.Service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    
    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher getVoucherById(int id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Voucher not found with id: " + id));
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Voucher not found with code: " + code));
    }

    @Override
    public Voucher createVoucher(Voucher voucher) {
        voucher.setCreatedAt(LocalDateTime.now());
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher updateVoucher(Voucher voucher) {
        if (!voucherRepository.existsById(voucher.getId())) {
            throw new BadRequestException("Voucher not found with id: " + voucher.getId());
        }
        return voucherRepository.save(voucher);
    }

    @Override
    public void deleteVoucher(int id) {
        if (!voucherRepository.existsById(id)) {
            throw new BadRequestException("Voucher not found with id: " + id);
        }
        voucherRepository.deleteById(id);
    }

    @Override
    public boolean isVoucherValid(String code) {
        return voucherRepository.findValidVoucherByCode(code, LocalDateTime.now()).isPresent();
    }

    @Override
    public boolean isVoucherValidForUser(String code, User user) {
        Voucher voucher = voucherRepository.findValidVoucherByCode(code, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired voucher"));
        
        // Check if user has already used this voucher
        return !userVoucherRepository.existsByUserAndVoucher(user, voucher);
    }

    @Override
    @Transactional
    public Voucher applyVoucher(String code, User user) {
        Voucher voucher = voucherRepository.findValidVoucherByCode(code, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired voucher"));
        
        // Check if user has already used this voucher
        if (userVoucherRepository.existsByUserAndVoucher(user, voucher)) {
            throw new BadRequestException("You have already used this voucher");
        }
        
        // Create user voucher record
        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        userVoucher.setUsedAt(LocalDateTime.now());
        userVoucherRepository.save(userVoucher);
        
        // Increment voucher usage count
        incrementVoucherUsage(voucher);
        
        return voucher;
    }

    @Override
    @Transactional
    public void incrementVoucherUsage(Voucher voucher) {
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
    }
}
