package org.gb.stellarplayer.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Repository.UserRepository;
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
    private final UserRepository userRepository;

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
     * Scheduled task to clean up unverified users older than 24 hours
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupUnverifiedUsers() {
        log.info("Running scheduled task to cleanup unverified users at {}", LocalDateTime.now());

        try {
            // Calculate the cutoff time (24 hours ago)
            LocalDateTime cutoffTime = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            
            // Find all unverified users created before the cutoff time
            List<Integer> unverifiedUserIds = userRepository.findUnverifiedUsersOlderThan(cutoffTime);
            
            if (!unverifiedUserIds.isEmpty()) {
                log.info("Found {} unverified users to delete", unverifiedUserIds.size());
                
                // Delete unverified users
                int deletedCount = userRepository.deleteUnverifiedUsersOlderThan(cutoffTime);
                
                log.info("Successfully deleted {} unverified users older than 24 hours", deletedCount);
            } else {
                log.info("No unverified users found to delete");
            }
            
        } catch (Exception e) {
            log.error("Error cleaning up unverified users: {}", e.getMessage(), e);
        }
    }

}
