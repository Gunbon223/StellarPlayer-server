package org.gb.stellarplayer.REST;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.OrderRequest;
import org.gb.stellarplayer.Response.OrderResponse;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Service.UserService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for user order history - users can only access their own orders
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderApi {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    /**
     * Get current user's order history
     * @param token Authentication token
     * @return List of user's orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(@RequestHeader("Authorization") String token) {
        User user = validateAndGetUser(token);
        
        // Get orders for the authenticated user
        List<Order> orders = orderService.getOrdersByUser(user);
        
        // Convert to response objects
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Get specific order by ID - user can only access their own orders
     * @param id Order ID
     * @param token Authentication token
     * @return Order details
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable int id, 
                                                     @RequestHeader("Authorization") String token) {
        User user = validateAndGetUser(token);
        
        // Get order
        Order order = orderService.getOrderById(id);
        
        // Check if user owns this order or is admin
        if (!order.getUser().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new BadRequestException("Access denied. You can only access your own orders");
        }
        
        // Convert to response object
        OrderResponse response = convertToResponse(order);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get orders by user ID - for admin use or self-access
     * @param userId User ID
     * @param token Authentication token
     * @return List of orders for the specified user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable int userId,
                                                                @RequestHeader("Authorization") String token) {
        validateUserAccess(token, userId);
        
        User targetUser = userService.getUserById(userId);
        List<Order> orders = orderService.getOrdersByUser(targetUser);
        
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
    
    /**
     * Create a new order for the authenticated user
     * @param orderRequest Order creation request
     * @param token Authentication token
     * @return Created order
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest,
                                                    @RequestHeader("Authorization") String token) {
        User user = validateAndGetUser(token);
        
        // Ensure user can only create orders for themselves (unless admin)
        if (orderRequest.getUserId() != user.getId() && !hasAdminRole(user)) {
            throw new BadRequestException("You can only create orders for yourself");
        }
        
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
    
    /**
     * Generate payment URL for an order
     * @param id Order ID
     * @param baseUrl Base URL for payment callback
     * @param token Authentication token
     * @return Payment URL
     */
    @PostMapping("/{id}/payment")
    public ResponseEntity<String> generatePaymentUrl(@PathVariable int id,
                                                    @RequestParam String baseUrl,
                                                    @RequestHeader("Authorization") String token) {
        User user = validateAndGetUser(token);
        
        // Get order and validate ownership
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new BadRequestException("Access denied. You can only generate payment for your own orders");
        }
        
        // Generate payment URL
        String paymentUrl = orderService.generatePaymentUrl(order, baseUrl);
        
        return new ResponseEntity<>(paymentUrl, HttpStatus.OK);
    }

    /**
     * Validate token and get user - similar to UserInfoApi pattern
     * @param token JWT token
     * @return Authenticated user
     */
    private User validateAndGetUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                return userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    /**
     * Validate user access - user can access their own data or admin can access any
     * @param token JWT token
     * @param userId Target user ID
     */
    private void validateUserAccess(String token, int userId) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                if (!hasAdminRole(user) && user.getId() != userId) {
                    throw new BadRequestException("Access denied. You can only access your own orders");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JWT token: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid token format");
        }
    }

    /**
     * Check if user has admin role
     * @param user User entity
     * @return True if user has admin role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
    }
    
    /**
     * Helper method to convert Order to OrderResponse
     * @param order Order entity
     * @return OrderResponse DTO
     */
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
