package org.gb.stellarplayer.Service.Implement;

import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service to automatically clean up old history records
 * to prevent database bloat and maintain optimal performance
 */
@Service
@RequiredArgsConstructor
public class ScheduledHistoryCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledHistoryCleanupService.class);
    
    private final HistoryService historyService;
    
    /**
     * Daily cleanup of old history records (runs at 2:00 AM)
     * Keeps records for 90 days by default
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2:00 AM
    public void dailyHistoryCleanup() {
        try {
            logger.info("Starting daily history cleanup...");
            
            long totalBefore = historyService.getHistoryCount();
            
            // Only run cleanup if there are more than 10,000 records
            if (totalBefore > 10000) {
                long deletedCount = historyService.deleteHistoryOlderThan(90);
                long totalAfter = historyService.getHistoryCount();
                
                if (deletedCount > 0) {
                    logger.info("Daily cleanup completed: {} records deleted, {} remaining", 
                               deletedCount, totalAfter);
                } else {
                    logger.info("Daily cleanup completed: No old records found for deletion");
                }
            } else {
                logger.info("Daily cleanup skipped: Only {} records in database (threshold: 10,000)", 
                           totalBefore);
            }
        } catch (Exception e) {
            logger.error("Error during daily history cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Weekly deep cleanup (runs every Sunday at 3:00 AM)
     * More aggressive cleanup for older records
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Every Sunday at 3:00 AM
    public void weeklyDeepCleanup() {
        try {
            logger.info("Starting weekly deep history cleanup...");
            
            long totalBefore = historyService.getHistoryCount();
            
            // Keep only 60 days of history in weekly cleanup
            long deletedCount = historyService.deleteHistoryOlderThan(60);
            long totalAfter = historyService.getHistoryCount();
            
            if (deletedCount > 0) {
                double spaceSavedPercent = (deletedCount * 100.0) / totalBefore;
                logger.info("Weekly deep cleanup completed:");
                logger.info("- Records before: {}", totalBefore);
                logger.info("- Records deleted: {}", deletedCount);
                logger.info("- Records after: {}", totalAfter);
                logger.info("- Space saved: {:.2f}%", spaceSavedPercent);
            } else {
                logger.info("Weekly deep cleanup completed: No old records found for deletion");
            }
        } catch (Exception e) {
            logger.error("Error during weekly deep history cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Emergency cleanup when database gets too large (runs every hour)
     * Triggered when record count exceeds 500,000
     */
    @Scheduled(fixedRate = 3600000) // Every hour (3,600,000 ms)
    public void emergencyCleanupCheck() {
        try {
            long totalRecords = historyService.getHistoryCount();
            
            // Emergency threshold: 500,000 records
            if (totalRecords > 500000) {
                logger.warn("Emergency cleanup triggered: {} records exceed threshold (500,000)", 
                           totalRecords);
                
                // Keep only 30 days in emergency cleanup
                long deletedCount = historyService.deleteHistoryOlderThan(30);
                long totalAfter = historyService.getHistoryCount();
                
                logger.warn("Emergency cleanup completed: {} records deleted, {} remaining", 
                           deletedCount, totalAfter);
                
                // Log statistics for monitoring
                var stats = historyService.getHistoryStatistics();
                logger.warn("Post-emergency stats: {}", stats);
            }
        } catch (Exception e) {
            logger.error("Error during emergency cleanup check: {}", e.getMessage(), e);
        }
    }
} 