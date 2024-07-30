package org.gb.stellarplayer.Controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.gb.stellarplayer.Entites.Role;
import org.gb.stellarplayer.Entites.User;
import org.gb.stellarplayer.Model.Enum.EnumUserRole;
import org.gb.stellarplayer.Repository.RoleRepository;
import org.gb.stellarplayer.Repository.UserRepository;
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
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }



}
