package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.Voucher;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.VoucherRequest;
import org.gb.stellarplayer.Response.VoucherResponse;
import org.gb.stellarplayer.Service.VoucherService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherApi {
    
    private final VoucherService voucherService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAllVouchers() {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        List<VoucherResponse> responses = vouchers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable int id) {
        Voucher voucher = voucherService.getVoucherById(id);
        VoucherResponse response = convertToResponse(voucher);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@RequestBody VoucherRequest voucherRequest,
                                                        @RequestHeader("Authorization") String token) {
        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        
        // Create voucher
        Voucher voucher = new Voucher();
        voucher.setCode(voucherRequest.getCode());
        voucher.setUsedCount(0);
        voucher.setMaxUseCount(voucherRequest.getMaxUseCount());
        voucher.setDiscountPercentage(voucherRequest.getDiscountPercentage());
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setStartDate(voucherRequest.getStartDate());
        voucher.setEndDate(voucherRequest.getEndDate());
        
        Voucher savedVoucher = voucherService.createVoucher(voucher);
        VoucherResponse response = convertToResponse(savedVoucher);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateVoucher(@RequestParam String code,
                                                             @RequestParam(required = false) Integer userId,
                                                             @RequestHeader("Authorization") String token) {
        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = voucherService.isVoucherValid(code);
            response.put("valid", isValid);
            
            if (isValid && userId != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                boolean isValidForUser = voucherService.isVoucherValidForUser(code, user);
                response.put("validForUser", isValidForUser);
                
                if (isValidForUser) {
                    Voucher voucher = voucherService.getVoucherByCode(code);
                    response.put("discountPercentage", voucher.getDiscountPercentage());
                }
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // Helper method to convert Voucher to VoucherResponse
    private VoucherResponse convertToResponse(Voucher voucher) {
        VoucherResponse response = new VoucherResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setUsedCount(voucher.getUsedCount());
        response.setMaxUseCount(voucher.getMaxUseCount());
        response.setDiscountPercentage(voucher.getDiscountPercentage());
        response.setStartDate(voucher.getStartDate());
        response.setEndDate(voucher.getEndDate());
        response.setActive(
                voucher.getStartDate().isBefore(LocalDateTime.now()) &&
                voucher.getEndDate().isAfter(LocalDateTime.now()) &&
                voucher.getUsedCount() < voucher.getMaxUseCount()
        );
        
        return response;
    }
}
