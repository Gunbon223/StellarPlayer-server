package org.gb.stellarplayer.Service;

import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.TrackPlayLog;
import org.gb.stellarplayer.Repository.TrackPlayLogRepository;
import org.gb.stellarplayer.Repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TrackPlayService {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private TrackPlayLogRepository trackPlayLogRepository;

    private static final int MAX_PLAYS_PER_DAY = 100;
    private static final int MIN_PLAY_INTERVAL_SECONDS = 30;
    private static final int SUSPICIOUS_PLAYS_THRESHOLD = 50;

    @Transactional
    public void recordPlay(Integer trackId, String ipAddress, Integer listenDuration) {
        Track track = trackRepository.findById(trackId)
            .orElseThrow(() -> new RuntimeException("Track not found"));

        LocalDateTime now = LocalDateTime.now();
        TrackPlayLog playLog = trackPlayLogRepository.findByTrackIdAndIpAddress(trackId, ipAddress)
            .orElseGet(() -> TrackPlayLog.builder()
                .track(track)
                .ipAddress(ipAddress)
                .playCount(0)
                .dailyPlayCount(0)
                .lastDailyReset(now)
                .build());

        // Check if IP is blocked
        if (playLog.getIsBlocked()) {
            throw new RuntimeException("This IP has been blocked: " + playLog.getBlockReason());
        }

        // Reset daily count if it's a new day
        if (playLog.getLastDailyReset() == null || 
            !playLog.getLastDailyReset().toLocalDate().equals(now.toLocalDate())) {
            playLog.setDailyPlayCount(0);
            playLog.setLastDailyReset(now);
        }

        // Check for minimum listen duration
        if (listenDuration < MIN_PLAY_INTERVAL_SECONDS) {
            playLog.setIsBlocked(true);
            playLog.setBlockReason("Play duration too short: " + listenDuration + " seconds");
            trackPlayLogRepository.save(playLog);
            return;
        }

        // Check daily play limit
        if (playLog.getDailyPlayCount() >= MAX_PLAYS_PER_DAY) {
            playLog.setIsBlocked(true);
            playLog.setBlockReason("Exceeded daily play limit");
            trackPlayLogRepository.save(playLog);
            return;
        }

        // Check for rapid plays
        if (playLog.getLastPlayedAt() != null && 
            ChronoUnit.SECONDS.between(playLog.getLastPlayedAt(), now) < MIN_PLAY_INTERVAL_SECONDS) {
            playLog.setIsBlocked(true);
            playLog.setBlockReason("Rapid play detected");
            trackPlayLogRepository.save(playLog);
            return;
        }

        // Check for suspicious activity
        List<TrackPlayLog> recentPlays = trackPlayLogRepository.findRecentPlaysByTrackAndIp(
            trackId, 
            ipAddress, 
            now.minus(1, ChronoUnit.HOURS)
        );
        
        if (recentPlays.size() > SUSPICIOUS_PLAYS_THRESHOLD) {
            playLog.setIsBlocked(true);
            playLog.setBlockReason("Suspicious activity detected: too many plays in short time");
            trackPlayLogRepository.save(playLog);
            return;
        }

        // If all checks pass, update the counts
        playLog.setPlayCount(playLog.getPlayCount() + 1);
        playLog.setDailyPlayCount(playLog.getDailyPlayCount() + 1);
        playLog.setLastPlayedAt(now);
        trackPlayLogRepository.save(playLog);

        // Update track's total play count
        track.setPlayCount(track.getPlayCount() + 1);
        track.setLastPlayedAt(now);
        trackRepository.save(track);
    }
} 