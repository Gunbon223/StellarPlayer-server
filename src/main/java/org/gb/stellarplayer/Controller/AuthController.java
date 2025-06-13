package org.gb.stellarplayer.Controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.Role;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Model.Enum.EnumUserRole;
import org.gb.stellarplayer.Repository.RoleRepository;
import org.gb.stellarplayer.Repository.UserRepository;
import org.gb.stellarplayer.Service.EmailService;
import org.gb.stellarplayer.Service.UserSubscriptionService;
import org.gb.stellarplayer.Service.VerificationTokenService;
import org.gb.stellarplayer.Request.LoginRequest;
import org.gb.stellarplayer.Request.RegisterRequest;
import org.gb.stellarplayer.Response.JwtResponse;
import org.gb.stellarplayer.Service.Implement.UserDetailsImplement;
import org.gb.stellarplayer.Ultils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@NoArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserSubscriptionService userSubscriptionService;
    @Autowired
    private VerificationTokenService verificationTokenService;
    @Autowired
    private EmailService emailService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);
        UserDetailsImplement userDetails = (UserDetailsImplement) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();
        JwtResponse res = new JwtResponse(jwt, "Bearer", userDetails.getId(), userDetails.getUsername(), roles);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByName(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        List<Role> roles = new ArrayList<>();
        Optional<Role> userRole = roleRepository.findByName(EnumUserRole.USER);
        if(userRole.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Role is not found!");
        }
        roles.add(userRole.get());
        User user = new User();
        user.setName(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setRoles(roles);
        user.setAvatar("https://placehold.co/100/ddddd/FFF?text="+registerRequest.getUsername().charAt(0));
        user.setEnabled(false); // User is disabled until email verification
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // Generate verification token and send email
        try {
            String verificationToken = verificationTokenService.generateVerificationToken(
                savedUser.getEmail(), 
                savedUser.getId()
            );
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
            
            return ResponseEntity.ok("User registered successfully! Please check your email to verify your account.");
        } catch (Exception e) {
            // If email sending fails, we should still return success but mention email issue
            return ResponseEntity.ok("User registered successfully! However, there was an issue sending the verification email. Please contact support.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        try {
            // Validate the token
            if (!verificationTokenService.validateVerificationToken(token)) {
                return ResponseEntity.badRequest().body("Invalid or expired verification token!");
            }

            // Check if token is expired
            if (verificationTokenService.isTokenExpired(token)) {
                return ResponseEntity.badRequest().body("Verification token has expired!");
            }

            // Get user from token
            String email = verificationTokenService.getEmailFromToken(token);
            Integer userId = verificationTokenService.getUserIdFromToken(token);
            String tokenType = verificationTokenService.getTokenTypeFromToken(token);

            // Verify it's an email verification token
            if (!"EMAIL_VERIFICATION".equals(tokenType)) {
                return ResponseEntity.badRequest().body("Invalid token type!");
            }

            // Find and update user
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found!");
            }

            User user = userOptional.get();
            
            // Check if email matches
            if (!user.getEmail().equals(email)) {
                return ResponseEntity.badRequest().body("Token email mismatch!");
            }

            // Check if already verified
            if (user.getEnabled()) {
                // Redirect to frontend success page with already verified message
                return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/auth/verification-success?status=already-verified")
                    .build();
            }

            // Enable the user
            user.setEnabled(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Send welcome email
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            } catch (Exception e) {
                // Log error but don't fail the verification
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }

            // Redirect to frontend success page
            return ResponseEntity.status(302)
                .header("Location", "http://localhost:3000/auth/verification-success?status=verified")
                .build();

        } catch (Exception e) {
            return ResponseEntity.status(302)
                .header("Location", "http://localhost:3000/auth/verification-success?status=error")
                .build();
        }
    }
}
