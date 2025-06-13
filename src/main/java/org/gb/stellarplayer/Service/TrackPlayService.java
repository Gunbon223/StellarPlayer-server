package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.TrackPlayLog;
import org.gb.stellarplayer.Repository.TrackPlayLogRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TrackPlayService {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private TrackPlayLogRepository trackPlayLogRepository;

    /**
     * Record a play for a track by an authenticated user
     * @param trackId ID of the track being played
     * @param ipAddress IP address of the client
     * @param listenDuration Duration of the listen in seconds
     * @param userId ID of the authenticated user
     */
    @Transactional
    public void recordPlay(Integer trackId, String ipAddress, Integer listenDuration, Integer userId) {
        Track track = trackRepository.findById(trackId)
            .orElseThrow(() -> new RuntimeException("Track not found"));

        LocalDateTime now = LocalDateTime.now();
        
        // Find or create play log for this user/track/IP combination
        TrackPlayLog playLog = trackPlayLogRepository.findByTrackIdAndIpAddress(trackId, ipAddress)
            .orElseGet(() -> TrackPlayLog.builder()
                .track(track)
                .ipAddress(ipAddress)
                .playCount(0)
                .dailyPlayCount(0)
                .lastDailyReset(now)
                .build());

        // Reset daily count if it's a new day
        if (playLog.getLastDailyReset() == null || 
            !playLog.getLastDailyReset().toLocalDate().equals(now.toLocalDate())) {
            playLog.setDailyPlayCount(0);
            playLog.setLastDailyReset(now);
        }

        // Basic validation - minimum listen duration
        if (listenDuration < 1) {
            throw new RuntimeException("Invalid listen duration. Must be at least 1 second.");
        }

        // Update the play counts
        playLog.setPlayCount(playLog.getPlayCount() + 1);
        playLog.setDailyPlayCount(playLog.getDailyPlayCount() + 1);
        playLog.setLastPlayedAt(now);
        trackPlayLogRepository.save(playLog);

        // Update track's total play count
        track.setPlayCount(track.getPlayCount() + 1);
        track.setLastPlayedAt(now);
        trackRepository.save(track);
    }

    /**
     * Legacy method for backward compatibility - throws exception
     * @deprecated Use recordPlay(Integer, String, Integer, Integer) instead
     */
    @Deprecated
    public void recordPlay(Integer trackId, String ipAddress, Integer listenDuration) {
        throw new RuntimeException("Authentication required. Users must be logged in to record plays.");
    }
} 