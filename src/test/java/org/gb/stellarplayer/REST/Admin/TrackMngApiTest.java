package org.gb.stellarplayer.REST.Admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gb.stellarplayer.DTO.TrackAdminDTO;
import org.gb.stellarplayer.Entites.Track;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Request.TrackRequest;
import org.gb.stellarplayer.Service.TrackService;
import org.gb.stellarplayer.Service.TrackStatsService;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackMngApi.class)
public class TrackMngApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrackService trackService;

    @MockBean
    private TrackStatsService trackStatsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    private String validToken;
    private User adminUser;
    private User artistUser;
    private Track testTrack;

    @BeforeEach
    void setUp() {
        // Setup test data
        validToken = "Bearer valid.jwt.token";
        adminUser = new User();
        adminUser.setName("admin");
        adminUser.setRoles(Arrays.asList(User.Role.ADMIN));

        artistUser = new User();
        artistUser.setName("artist");
        artistUser.setRoles(Arrays.asList(User.Role.ARTIST));

        testTrack = new Track();
        testTrack.setId(1);
        testTrack.setTitle("Test Track");
        testTrack.setDuration(180);
        testTrack.setPath("/path/to/track");
        testTrack.setCover("/path/to/cover");
        testTrack.setLyrics("Test lyrics");
        testTrack.setStatus(true);
        testTrack.setPlayCount(0L);

        // Mock JWT validation
//        when(jwtUtil.validateJwtToken(any())).thenReturn(true);
        when(jwtUtil.getUserNameFromJwtToken(any())).thenReturn("admin");
        when(jwtUtil.hasAdminRole(any())).thenReturn(true);
        when(jwtUtil.hasArtistRole(any())).thenReturn(false);

        // Mock user repository
        when(userRepository.findByName("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByName("artist")).thenReturn(Optional.of(artistUser));
    }

    @Test
    void getTrack_WithValidToken_ShouldReturnTrack() throws Exception {
        when(trackService.getTrackById(anyInt())).thenReturn(testTrack);

        mockMvc.perform(get("/api/admin/track/1")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Track"));
    }

    @Test
    void getAllTracks_WithValidToken_ShouldReturnTracks() throws Exception {
        List<Track> tracks = Arrays.asList(testTrack);
        when(trackService.getTracks()).thenReturn(tracks);

        mockMvc.perform(get("/api/admin/track")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Track"));
    }

    @Test
    void createTrack_WithValidToken_ShouldCreateTrack() throws Exception {
        TrackRequest trackRequest = new TrackRequest();
        trackRequest.setTitle("New Track");
        trackRequest.setDuration(200);
        trackRequest.setPath("/path/to/new/track");
        trackRequest.setCover("/path/to/new/cover");
        trackRequest.setLyrics("New lyrics");
        trackRequest.setStatus(true);
        trackRequest.setPlayCount(0L);

        when(trackService.saveTrack(any(Track.class))).thenReturn(testTrack);

        mockMvc.perform(post("/api/admin/track")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Track"));
    }

    @Test
    void updateTrack_WithValidToken_ShouldUpdateTrack() throws Exception {
        TrackRequest trackRequest = new TrackRequest();
        trackRequest.setTitle("Updated Track");
        trackRequest.setDuration(200);
        trackRequest.setPath("/path/to/updated/track");
        trackRequest.setCover("/path/to/updated/cover");
        trackRequest.setLyrics("Updated lyrics");
        trackRequest.setStatus(true);
        trackRequest.setPlayCount(0L);

        when(trackService.getTrackById(anyInt())).thenReturn(testTrack);
        when(trackService.updateTrack(any(Track.class))).thenReturn(testTrack);

        mockMvc.perform(put("/api/admin/track/1")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Track"));
    }

    @Test
    void deleteTrack_WithValidToken_ShouldDeleteTrack() throws Exception {
        mockMvc.perform(delete("/api/admin/track/1")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Track deleted successfully"));
    }

    @Test
    void getTrack_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/track/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTrack_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        when(jwtUtil.validateJwtToken(any())).thenReturn(false);

        mockMvc.perform(get("/api/admin/track/1")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTrack_WithArtistToken_ShouldReturnTrack() throws Exception {
        when(jwtUtil.hasAdminRole(any())).thenReturn(false);
        when(jwtUtil.hasArtistRole(any())).thenReturn(true);
        when(trackService.getTrackById(anyInt())).thenReturn(testTrack);

        mockMvc.perform(get("/api/admin/track/1")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Track"));
    }

    @Test
    void debugToken_WithValidToken_ShouldReturnTokenInfo() throws Exception {
        mockMvc.perform(get("/api/admin/track/token-debug")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.isArtist").value(false))
                .andExpect(jsonPath("$.isAuthorized").value(true));
    }
} 