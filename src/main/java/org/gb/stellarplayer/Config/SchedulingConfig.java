package org.gb.stellarplayer.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Service.OrderService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Configuration class for scheduling tasks
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Log4j2
public class SchedulingConfig {

    private final OrderService orderService;
    private final RestTemplate restTemplate;

    /**
     * Scheduled task to update pending orders
     * Runs every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    public void updatePendingOrders() {
        log.info("Running scheduled task to update pending orders at {}", LocalDateTime.now());

        try {
            // Call the API endpoint to update pending orders
            String apiUrl = "http://localhost:8080/api/v1/payments/check-pending-orders";
            restTemplate.getForObject(apiUrl, String.class);

            log.info("Successfully updated pending orders");
        } catch (Exception e) {
            log.error("Error updating pending orders: {}", e.getMessage(), e);
        }
    }

    /**
     * Alternative implementation that directly updates orders without calling the API
     * This can be used if you prefer not to make HTTP requests within the application
     * Uncomment this method and comment out the updatePendingOrders method if you prefer this approach
     */
    /*
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    public void updatePendingOrdersDirectly() {
        log.info("Running scheduled task to directly update pending orders at {}", LocalDateTime.now());

        try {
            // Get all orders
            List<org.gb.stellarplayer.Entites.Order> allOrders = orderService.getAllOrders();
            int updatedCount = 0;

            // Filter for pending orders that are older than 15 minutes
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);

            for (org.gb.stellarplayer.Entites.Order order : allOrders) {
                // Check if the order is pending and created more than 15 minutes ago
                if ("PENDING".equals(order.getStatus()) &&
                    order.getCreatedAt().isBefore(fifteenMinutesAgo)) {

                    // Update the order status to CANCELLED
                    orderService.updateOrderStatus(order.getOrderCode(), "CANCELLED", null);
                    updatedCount++;
                }
            }

            log.info("Successfully updated {} pending orders to cancelled", updatedCount);
        } catch (Exception e) {
            log.error("Error directly updating pending orders: {}", e.getMessage(), e);
        }
    }
    */
}
