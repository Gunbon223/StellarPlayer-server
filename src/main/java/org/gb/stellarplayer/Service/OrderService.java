package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.User;

import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();
    List<Order> getOrdersByUser(User user);
    Order getOrderById(int id);
    Order getOrderByCode(String orderCode);
    
    Order createOrder(int userId, int subscriptionId, String voucherCode);
    Order updateOrderStatus(String orderCode, String status, String transactionId);
    
    // Payment related methods
    String generatePaymentUrl(Order order, String baseUrl);
    boolean processPaymentReturn(String orderCode, String transactionStatus, String transactionId);
}
