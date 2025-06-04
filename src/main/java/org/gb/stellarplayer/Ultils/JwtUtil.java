package org.gb.stellarplayer.Ultils;

import io.jsonwebtoken.*;
import org.gb.stellarplayer.Exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.gb.stellarplayer.Service.Implement.UserDetailsImplement;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.SignatureException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
public class JwtUtil {
    @Value("${stellarplayer.app.jwtSecret}")
    private String secret;

    @Value("${stellarplayer.app.jwtExpirationMs}")
    private long expirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImplement userDetails = (UserDetailsImplement) authentication.getPrincipal();

        // Extract roles and ensure they have ROLE_ prefix
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> {
                    String role = item.getAuthority();
                    return role.startsWith("ROLE_") ? role : "ROLE_" + role;
                })
                .toList();
        
        log.debug("Generating JWT token for user: {} with roles: {}", userDetails.getUsername(), roles);

        return Jwts.builder()
                .setSubject((userDetails.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .claim("roles", roles)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        String username = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
        log.debug("Extracted username from token: {}", username);
        return username;
    }

    public void validateJwtToken(String authToken) {
        log.debug("Validating JWT token...");
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken).getBody();
            log.debug("Token validation successful. Claims: {}", claims);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new BadRequestException("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new BadRequestException("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new BadRequestException("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new BadRequestException("JWT claims string is empty: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            List<String> roles = (List<String>) claims.get("roles");
            // Ensure roles have ROLE_ prefix
            roles = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());
            log.debug("Extracted roles from token: {}", roles);
            return roles != null ? roles : List.of();
        } catch (Exception e) {
            log.error("Error getting roles from token: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean hasAdminRole(String token) {
        List<String> roles = getRolesFromJwtToken(token);
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        log.debug("Checking admin role. Roles: {}, Is Admin: {}", roles, isAdmin);
        return isAdmin;
    }

    public boolean hasArtistRole(String token) {
        List<String> roles = getRolesFromJwtToken(token);
        boolean isArtist = roles.contains("ROLE_ARTIST");
        log.debug("Checking artist role. Roles: {}, Is Artist: {}", roles, isArtist);
        return isArtist;
    }
}
