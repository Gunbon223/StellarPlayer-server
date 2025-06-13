package org.gb.stellarplayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TrackApiLoadTest {

    @LocalServerPort
    private int port;

    private WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Test configuration
    private static final int CONCURRENT_USERS = 1000;
    private static final int REQUESTS_PER_USER = 5;
    private static final int TEST_TRACK_ID = 6; // Change this to an existing track ID
    private static final Duration TEST_TIMEOUT = Duration.ofMinutes(5);

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Test
    void loadTestTrackPlayEndpoint() throws InterruptedException {
        System.out.println("🚀 Starting load test with " + CONCURRENT_USERS + " concurrent users");
        System.out.println("📊 Each user will make " + REQUESTS_PER_USER + " requests");
        System.out.println("🎯 Target endpoint: POST /api/v1/track/" + TEST_TRACK_ID + "/play");

        LoadTestMetrics metrics = new LoadTestMetrics();
        Instant startTime = Instant.now();

        // Create thread pool for concurrent execution
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);

        // Submit tasks for each concurrent user
        for (int userId = 0; userId < CONCURRENT_USERS; userId++) {
            final int finalUserId = userId;
            executor.submit(() -> {
                try {
                    simulateUserListeningSession(finalUserId, metrics);
                } catch (Exception e) {
                    metrics.recordError();
                    System.err.println("❌ User " + finalUserId + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all users to complete
        boolean completed = latch.await(TEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        executor.shutdown();

        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);

        // Print detailed results
        printLoadTestResults(metrics, totalDuration, completed);
    }

    @Test
    void loadTestTrackStatsEndpoint() throws InterruptedException {
        System.out.println("🚀 Starting load test for Track Stats endpoint");

        LoadTestMetrics metrics = new LoadTestMetrics();
        Instant startTime = Instant.now();

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);

        for (int userId = 0; userId < CONCURRENT_USERS; userId++) {
            final int finalUserId = userId;
            executor.submit(() -> {
                try {
                    simulateUserGettingStats(finalUserId, metrics);
                } catch (Exception e) {
                    metrics.recordError();
                    System.err.println("❌ User " + finalUserId + " stats request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(TEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        executor.shutdown();

        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);

        printLoadTestResults(metrics, totalDuration, completed);
    }

    @Test
    void loadTestMixedTrafficScenario() throws InterruptedException {
        System.out.println("🚀 Starting MIXED TRAFFIC load test (realistic music streaming simulation)");

        LoadTestMetrics playMetrics = new LoadTestMetrics();
        LoadTestMetrics statsMetrics = new LoadTestMetrics();
        LoadTestMetrics playCountMetrics = new LoadTestMetrics();

        Instant startTime = Instant.now();

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);

        for (int userId = 0; userId < CONCURRENT_USERS; userId++) {
            final int finalUserId = userId;
            executor.submit(() -> {
                try {
                    simulateRealisticUserBehavior(finalUserId, playMetrics, statsMetrics, playCountMetrics);
                } catch (Exception e) {
                    playMetrics.recordError();
                    System.err.println("❌ User " + finalUserId + " mixed scenario failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(TEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        executor.shutdown();

        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);

        // Print detailed results for mixed scenario
        printMixedTrafficResults(playMetrics, statsMetrics, playCountMetrics, totalDuration, completed);
    }

    private void simulateUserListeningSession(int userId, LoadTestMetrics metrics) {
        Random random = new Random();
        
        for (int request = 0; request < REQUESTS_PER_USER; request++) {
            try {
                Instant requestStart = Instant.now();
                
                // Simulate listening duration between 30-180 seconds
                int listenDuration = 30 + random.nextInt(151);
                
                String response = webClient.post()
                        .uri("/api/v1/track/{id}/play?listenDuration={duration}", TEST_TRACK_ID, listenDuration)
                        .header("X-Forwarded-For", generateRandomIp())
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(30))
                        .block();
                
                Instant requestEnd = Instant.now();
                long responseTime = Duration.between(requestStart, requestEnd).toMillis();
                
                metrics.recordSuccess(responseTime);
                
                if (userId % 100 == 0 && request == 0) {
                    System.out.println("✅ User " + userId + " completed request " + (request + 1) + 
                                     " (Response time: " + responseTime + "ms)");
                }
                
                // Simulate think time between requests (1-3 seconds)
                Thread.sleep(1000 + random.nextInt(2000));
                
            } catch (Exception e) {
                metrics.recordError();
                System.err.println("❌ User " + userId + " request " + (request + 1) + " failed: " + e.getMessage());
            }
        }
    }

    private void simulateUserGettingStats(int userId, LoadTestMetrics metrics) {
        Random random = new Random();

        for (int request = 0; request < REQUESTS_PER_USER; request++) {
            try {
                Instant requestStart = Instant.now();

                String response = webClient.get()
                        .uri("/api/v1/track/{id}/stats", TEST_TRACK_ID)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(30))
                        .block();

                Instant requestEnd = Instant.now();
                long responseTime = Duration.between(requestStart, requestEnd).toMillis();

                metrics.recordSuccess(responseTime);

                if (userId % 100 == 0 && request == 0) {
                    System.out.println("✅ User " + userId + " got stats (Response time: " + responseTime + "ms)");
                }

                Thread.sleep(500 + random.nextInt(1000)); // Shorter think time for stats

            } catch (Exception e) {
                metrics.recordError();
                System.err.println("❌ User " + userId + " stats request " + (request + 1) + " failed: " + e.getMessage());
            }
        }
    }

    private void simulateRealisticUserBehavior(int userId, LoadTestMetrics playMetrics,
                                               LoadTestMetrics statsMetrics, LoadTestMetrics playCountMetrics) {
        Random random = new Random();

        // Each user performs a mix of operations
        for (int session = 0; session < REQUESTS_PER_USER; session++) {
            try {
                // 60% chance to play a track
                if (random.nextDouble() < 0.6) {
                    simulatePlayRequest(userId, session, playMetrics);
                }
                // 25% chance to get stats
                else if (random.nextDouble() < 0.85) {
                    simulateStatsRequest(userId, session, statsMetrics);
                }
                // 15% chance to get play count
                else {
                    simulatePlayCountRequest(userId, session, playCountMetrics);
                }

                // Random delay between actions
                Thread.sleep(random.nextInt(2000) + 500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void simulatePlayRequest(int userId, int session, LoadTestMetrics metrics) {
        try {
            Instant requestStart = Instant.now();
            Random random = new Random();
            int listenDuration = 30 + random.nextInt(151);
            
            String response = webClient.post()
                    .uri("/api/v1/track/{id}/play?listenDuration={duration}", TEST_TRACK_ID, listenDuration)
                    .header("X-Forwarded-For", generateRandomIp())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            Instant requestEnd = Instant.now();
            long responseTime = Duration.between(requestStart, requestEnd).toMillis();
            metrics.recordSuccess(responseTime);
            
        } catch (Exception e) {
            metrics.recordError();
        }
    }

    private void simulateStatsRequest(int userId, int session, LoadTestMetrics metrics) {
        try {
            Instant requestStart = Instant.now();

            String response = webClient.get()
                    .uri("/api/v1/track/{id}/stats", TEST_TRACK_ID)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            Instant requestEnd = Instant.now();
            long responseTime = Duration.between(requestStart, requestEnd).toMillis();
            metrics.recordSuccess(responseTime);

        } catch (Exception e) {
            metrics.recordError();
        }
    }

    private void simulatePlayCountRequest(int userId, int session, LoadTestMetrics metrics) {
        try {
            Instant requestStart = Instant.now();

            String response = webClient.get()
                    .uri("/api/v1/track/{id}/playcount", TEST_TRACK_ID)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            Instant requestEnd = Instant.now();
            long responseTime = Duration.between(requestStart, requestEnd).toMillis();
            metrics.recordSuccess(responseTime);

        } catch (Exception e) {
            metrics.recordError();
        }
    }

    private String generateRandomIp() {
        Random random = new Random();
        return String.format("%d.%d.%d.%d",
                random.nextInt(256), random.nextInt(256),
                random.nextInt(256), random.nextInt(256));
    }

    private void printLoadTestResults(LoadTestMetrics metrics, Duration totalDuration, boolean completed) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 LOAD TEST RESULTS");
        System.out.println("=".repeat(80));

        if (!completed) {
            System.out.println("⚠️  TEST TIMED OUT - Results may be incomplete");
        }

        System.out.printf("🕐 Total Duration: %.2f seconds%n", totalDuration.toMillis() / 1000.0);
        System.out.printf("👥 Concurrent Users: %d%n", CONCURRENT_USERS);
        System.out.printf("📈 Total Requests: %d%n", metrics.getTotalRequests());
        System.out.printf("✅ Successful Requests: %d%n", metrics.getSuccessfulRequests());
        System.out.printf("❌ Failed Requests: %d%n", metrics.getFailedRequests());
        System.out.printf("🎯 Success Rate: %.2f%%%n", metrics.getSuccessRate());
        System.out.printf("🚀 Requests/Second: %.2f%n", metrics.getRequestsPerSecond(totalDuration));

        if (metrics.getSuccessfulRequests() > 0) {
            System.out.printf("⚡ Average Response Time: %.2f ms%n", metrics.getAverageResponseTime());
            System.out.printf("🏃 Min Response Time: %d ms%n", metrics.getMinResponseTime());
            System.out.printf("🐌 Max Response Time: %d ms%n", metrics.getMaxResponseTime());
            System.out.printf("📊 95th Percentile: %.2f ms%n", metrics.get95thPercentile());
        }

        System.out.println("=".repeat(80));
    }

    private void printMixedTrafficResults(LoadTestMetrics playMetrics, LoadTestMetrics statsMetrics,
                                          LoadTestMetrics playCountMetrics, Duration totalDuration, boolean completed) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 MIXED TRAFFIC LOAD TEST RESULTS");
        System.out.println("=".repeat(80));

        if (!completed) {
            System.out.println("⚠️  TEST TIMED OUT - Results may be incomplete");
        }

        System.out.printf("🕐 Total Duration: %.2f seconds%n", totalDuration.toMillis() / 1000.0);
        System.out.printf("👥 Concurrent Users: %d%n", CONCURRENT_USERS);

        System.out.println("\n🎵 PLAY ENDPOINT METRICS:");
        printMetricsSummary(playMetrics, totalDuration);

        System.out.println("\n📊 STATS ENDPOINT METRICS:");
        printMetricsSummary(statsMetrics, totalDuration);

        System.out.println("\n🔢 PLAYCOUNT ENDPOINT METRICS:");
        printMetricsSummary(playCountMetrics, totalDuration);

        // Overall metrics
        int totalRequests = playMetrics.getTotalRequests() + statsMetrics.getTotalRequests() + playCountMetrics.getTotalRequests();
        int totalSuccessful = playMetrics.getSuccessfulRequests() + statsMetrics.getSuccessfulRequests() + playCountMetrics.getSuccessfulRequests();
        double overallSuccessRate = totalRequests > 0 ? (totalSuccessful * 100.0 / totalRequests) : 0;
        double overallRps = totalRequests / (totalDuration.toMillis() / 1000.0);

        System.out.println("\n🎯 OVERALL METRICS:");
        System.out.printf("   Total Requests: %d%n", totalRequests);
        System.out.printf("   Success Rate: %.2f%%%n", overallSuccessRate);
        System.out.printf("   Overall RPS: %.2f%n", overallRps);

        System.out.println("=".repeat(80));
    }

    private void printMetricsSummary(LoadTestMetrics metrics, Duration totalDuration) {
        System.out.printf("   Requests: %d | Success: %d | Failed: %d%n",
                metrics.getTotalRequests(), metrics.getSuccessfulRequests(), metrics.getFailedRequests());
        System.out.printf("   Success Rate: %.2f%% | RPS: %.2f%n",
                metrics.getSuccessRate(), metrics.getRequestsPerSecond(totalDuration));
        if (metrics.getSuccessfulRequests() > 0) {
            System.out.printf("   Avg Response: %.2f ms | 95th Percentile: %.2f ms%n",
                    metrics.getAverageResponseTime(), metrics.get95thPercentile());
        }
    }

    // Metrics collection class
    private static class LoadTestMetrics {
        private final AtomicInteger successfulRequests = new AtomicInteger(0);
        private final AtomicInteger failedRequests = new AtomicInteger(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxResponseTime = new AtomicLong(0);
        private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

        public void recordSuccess(long responseTime) {
            successfulRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            responseTimes.add(responseTime);

            // Update min/max atomically
            minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
            maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
        }

        public void recordError() {
            failedRequests.incrementAndGet();
        }

        public int getSuccessfulRequests() { return successfulRequests.get(); }
        public int getFailedRequests() { return failedRequests.get(); }
        public int getTotalRequests() { return successfulRequests.get() + failedRequests.get(); }

        public double getSuccessRate() {
            int total = getTotalRequests();
            return total > 0 ? (successfulRequests.get() * 100.0 / total) : 0;
        }

        public double getAverageResponseTime() {
            int successful = successfulRequests.get();
            return successful > 0 ? (totalResponseTime.get() / (double) successful) : 0;
        }

        public long getMinResponseTime() {
            long min = minResponseTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }

        public long getMaxResponseTime() { return maxResponseTime.get(); }

        public double get95thPercentile() {
            List<Long> times = new ArrayList<>(responseTimes);
            if (times.isEmpty()) return 0;

            Collections.sort(times);
            int index = (int) Math.ceil(times.size() * 0.95) - 1;
            return index >= 0 ? times.get(index) : 0;
        }

        public double getRequestsPerSecond(Duration duration) {
            double seconds = duration.toMillis() / 1000.0;
            return seconds > 0 ? (getTotalRequests() / seconds) : 0;
        }
    }
}