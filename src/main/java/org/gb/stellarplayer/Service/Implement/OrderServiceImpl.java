package org.gb.stellarplayer.Service.Implement;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Config.VNPAYConfig;
import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.Subscription;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Entites.Voucher;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.OrderRepository;
import org.gb.stellarplayer.Repository.SubscriptionRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.gb.stellarplayer.Service.VNPAYService;
import org.gb.stellarplayer.Service.VoucherService;
import org.gb.stellarplayer.Ultils.MockHttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final VoucherService voucherService;
    private final UserSubscriptionService userSubscriptionService;
    private final VNPAYService vnpayService;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Order getOrderById(int id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Order not found with id: " + id));
    }

    @Override
    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BadRequestException("Order not found with code: " + orderCode));
    }

    @Override
    @Transactional
    public Order createOrder(int userId, int subscriptionId, String voucherCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BadRequestException("Subscription not found with id: " + subscriptionId));

        // Generate a unique order code
        String orderCode = UUID.randomUUID().toString().substring(0, 8);

        // Calculate original amount
        double originalAmount = subscription.getPrice();
        double discountAmount = 0.0;
        double finalAmount = originalAmount;

        // Apply voucher if provided
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            if (!voucherService.isVoucherValidForUser(voucherCode, user)) {
                throw new BadRequestException("Invalid voucher or already used");
            }

            voucher = voucherService.getVoucherByCode(voucherCode);
            discountAmount = originalAmount * (voucher.getDiscountPercentage() / 100.0);
            finalAmount = originalAmount - discountAmount;

            // Ensure final amount is not negative
            if (finalAmount < 0) {
                finalAmount = 0.0;
            }
        }

        // Create and save the order
        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .subscription(subscription)
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status("PENDING")
                .appliedVoucher(voucher)
                .createdAt(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderCode, String status, String transactionId) {
        Order order = getOrderByCode(orderCode);
        order.setStatus(status);

        if (status.equals("PAID")) {
            order.setPaidAt(LocalDateTime.now());
            order.setTransactionId(transactionId);

            // Apply voucher if used
            if (order.getAppliedVoucher() != null) {
                voucherService.applyVoucher(order.getAppliedVoucher().getCode(), order.getUser());
            }

            // Create user subscription
            userSubscriptionService.addUserSubscription(order.getUser().getId(), order.getSubscription().getId());
        } else if (status.equals("CANCELLED")) {
            order.setCancelledAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    @Override
    public String generatePaymentUrl(Order order, String baseUrl) {
        // Create payment URL using VNPAY service
        int amount = (int) Math.round(order.getFinalAmount());
        String orderInfo = "Payment for subscription: " + order.getSubscription().getName() + " - Order: " + order.getOrderCode();

        // Create a mock HttpServletRequest for VNPAY service
        HttpServletRequest mockRequest = new MockHttpServletRequest();

        return vnpayService.createOrder(mockRequest, amount, orderInfo, baseUrl);
    }

    @Override
    @Transactional
    public boolean processPaymentReturn(String orderCode, String transactionStatus, String transactionId) {
        if ("00".equals(transactionStatus)) {
            updateOrderStatus(orderCode, "PAID", transactionId);
            return true;
        } else {
            updateOrderStatus(orderCode, "CANCELLED", null);
            return false;
        }
    }


}
