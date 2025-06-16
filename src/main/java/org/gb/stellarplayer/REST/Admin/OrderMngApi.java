package org.gb.stellarplayer.REST.Admin;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Entites.Order;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Response.OrderResponse;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Service.UserService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Order management - accessible by admin role only
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderMngApi {
    private final OrderService orderService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Get all orders
     * @param token Authentication token
     * @return List of all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestHeader("Authorization") String token) {
        validatePermission(token);
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Get order by ID
     * @param id Order ID
     * @param token Authentication token
     * @return Order details
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable int id,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        Order order = orderService.getOrderById(id);
        OrderResponse response = convertToResponse(order);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get orders by user ID
     * @param userId User ID
     * @param token Authentication token
     * @return List of orders for the specified user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @PathVariable int userId,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            User user = userService.getUserById(userId);
            List<Order> orders = orderService.getOrdersByUser(user);
            List<OrderResponse> responses = orders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get user orders: " + e.getMessage());
        }
    }

    /**
     * Get orders by status
     * @param status Order status (PENDING, PAID, CANCELLED)
     * @param token Authentication token
     * @return List of orders with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            List<Order> allOrders = orderService.getAllOrders();
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                    .collect(Collectors.toList());
            
            List<OrderResponse> responses = filteredOrders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get orders by status: " + e.getMessage());
        }
    }

    /**
     * Get orders by date range
     * @param startDate Start date (YYYY-MM-DD format)
     * @param endDate End date (YYYY-MM-DD format)
     * @param token Authentication token
     * @return List of orders in the date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            
            List<Order> allOrders = orderService.getAllOrders();
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order -> order.getCreatedAt() != null)
                    .filter(order -> !order.getCreatedAt().isBefore(start) && !order.getCreatedAt().isAfter(end))
                    .collect(Collectors.toList());
            
            List<OrderResponse> responses = filteredOrders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get orders by date range: " + e.getMessage());
        }
    }

    /**
     * Get order by order code
     * @param orderCode Order code
     * @param token Authentication token
     * @return Order details
     */
    @GetMapping("/code/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderByCode(
            @PathVariable String orderCode,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            Order order = orderService.getOrderByCode(orderCode);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get order by code: " + e.getMessage());
        }
    }

    /**
     * Update order status
     * @param id Order ID
     * @param status New status
     * @param transactionId Transaction ID (optional)
     * @param token Authentication token
     * @return Updated order
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestParam String status,
            @RequestParam(required = false) String transactionId,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            // Get the order first to validate it exists
            Order order = orderService.getOrderById(id);
            
            // Update order status using the service method
            Order updatedOrder = orderService.updateOrderStatus(order.getOrderCode(), status, transactionId);
            OrderResponse response = convertToResponse(updatedOrder);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update order status: " + e.getMessage()));
        }
    }

    /**
     * Get order statistics
     * @param token Authentication token
     * @return Order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(@RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            List<Order> allOrders = orderService.getAllOrders();
            
            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
            long paidOrders = allOrders.stream().filter(o -> "PAID".equals(o.getStatus())).count();
            long cancelledOrders = allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();
            
            double totalRevenue = allOrders.stream()
                    .filter(o -> "PAID".equals(o.getStatus()))
                    .mapToDouble(Order::getFinalAmount)
                    .sum();
            
            Map<String, Object> statistics = Map.of(
                "totalOrders", totalOrders,
                "pendingOrders", pendingOrders,
                "paidOrders", paidOrders,
                "cancelledOrders", cancelledOrders,
                "totalRevenue", totalRevenue
            );
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            throw new BadRequestException("Failed to get order statistics: " + e.getMessage());
        }
    }

    /**
     * Search orders by user name or email
     * @param query Search query (user name or email)
     * @param token Authentication token
     * @return List of matching orders
     */
    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam String query,
            @RequestHeader("Authorization") String token) {
        validatePermission(token);
        try {
            List<Order> allOrders = orderService.getAllOrders();
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order -> order.getUser() != null)
                    .filter(order -> 
                        (order.getUser().getName() != null && order.getUser().getName().toLowerCase().contains(query.toLowerCase())) ||
                        (order.getUser().getEmail() != null && order.getUser().getEmail().toLowerCase().contains(query.toLowerCase())) ||
                        (order.getOrderCode() != null && order.getOrderCode().toLowerCase().contains(query.toLowerCase()))
                    )
                    .collect(Collectors.toList());
            
            List<OrderResponse> responses = filteredOrders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            throw new BadRequestException("Failed to search orders: " + e.getMessage());
        }
    }

    /**
     * Debug endpoint for token verification
     * @param token Authentication token
     * @return Token information
     */
    @GetMapping("/token-debug")
    public ResponseEntity<?> debugToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                
                // Check if token is valid
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                boolean isAdmin = jwtUtil.hasAdminRole(jwt);
                
                Map<String, Object> debug = Map.of(
                    "username", username,
                    "isAdmin", isAdmin,
                    "isAuthorized", isAdmin
                );
                return ResponseEntity.ok(debug);
            }
            return ResponseEntity.badRequest().body("Invalid token format");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token validation error: " + e.getMessage());
        }
    }

    /**
     * Validate token and check for admin role
     * @param token JWT token
     * @throws BadRequestException If token is invalid or user does not have admin permissions
     */
    private void validatePermission(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                jwtUtil.validateJwtToken(jwt);
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                User user = userRepository.findByName(username)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                
                if (!hasAdminRole(user)) {
                    throw new BadRequestException("Access denied. Admin privileges required");
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
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
    }

    /**
     * Helper method to convert Order entity to OrderResponse DTO
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