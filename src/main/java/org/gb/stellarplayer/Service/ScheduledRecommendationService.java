package org.gb.stellarplayer.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduledRecommendationService {

    private final RecommendationService recommendationService;

    // Update system playlists every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void updateSystemPlaylists() {
        recommendationService.updateSystemPlaylists();
    }

    // Update trending weekly every Monday at 3 AM
    @Scheduled(cron = "0 0 3 * * MON")
    public void updateWeeklyPlaylists() {
        recommendationService.generateTrendingWeekly();
    }

    // Update all user artist radio playlists once a week (Sunday at 4 AM)
    @Scheduled(cron = "0 0 4 * * SUN")
    public void updateArtistRadioPlaylists() {
        try {
            recommendationService.updateAllUserArtistRadios();
        } catch (Exception e) {
            System.err.println("Error updating artist radio playlists: " + e.getMessage());
        }
    }

    // Check for stale artist radio playlists that need refresh every day at 5 AM
    @Scheduled(cron = "0 0 5 * * *")
    public void checkStaleArtistRadios() {
        try {
            recommendationService.updateAllUserArtistRadios();
        } catch (Exception e) {
            System.err.println("Error checking stale artist radios: " + e.getMessage());
        }
    }
}