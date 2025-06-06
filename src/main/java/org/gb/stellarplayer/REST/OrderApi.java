package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.OrderRequest;
import org.gb.stellarplayer.Response.OrderResponse;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderApi {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestHeader("Authorization") String token) {
        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        
        // Get username from token
        String username = jwtUtil.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        // Get orders for the user
        List<Order> orders = orderService.getOrdersByUser(user);
        
        // Convert to response objects
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable int id, 
                                                     @RequestHeader("Authorization") String token) {
        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        
        // Get order
        Order order = orderService.getOrderById(id);
        
        // Convert to response object
        OrderResponse response = convertToResponse(order);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest,
                                                    @RequestHeader("Authorization") String token) {
        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        
        // Create order
        Order order = orderService.createOrder(
                orderRequest.getUserId(),
                orderRequest.getSubscriptionId(),
                orderRequest.getVoucherCode()
        );
        
        // Convert to response object
        OrderResponse response = convertToResponse(order);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/{id}/payment")
    public ResponseEntity<String> generatePaymentUrl(@PathVariable int id,
                                                    @RequestParam String baseUrl,
                                                    @RequestHeader("Authorization") String token) {
        // Validate JWT token
        String jwt = token.substring(7);
        jwtUtil.validateJwtToken(jwt);
        
        // Get order
        Order order = orderService.getOrderById(id);
        
        // Generate payment URL
        String paymentUrl = orderService.generatePaymentUrl(order, baseUrl);
        
        return new ResponseEntity<>(paymentUrl, HttpStatus.OK);
    }
    
    // Helper method to convert Order to OrderResponse
    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
        response.setUserId(order.getUser().getId());
        response.setSubscriptionId(order.getSubscription().getId());
        response.setSubscriptionName(order.getSubscription().getName());
        response.setOriginalAmount(order.getOriginalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setPaidAt(order.getPaidAt());
        
        if (order.getAppliedVoucher() != null) {
            response.setVoucherCode(order.getAppliedVoucher().getCode());
            response.setVoucherDiscount(order.getAppliedVoucher().getDiscountPercentage());
        }
        
        return response;
    }
}
