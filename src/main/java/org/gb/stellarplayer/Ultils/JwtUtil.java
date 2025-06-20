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

@Component
@Log4j2
public class JwtUtil {
    @Value("${stellarplayer.app.jwtSecret}")
    private String secret;

    @Value("${stellarplayer.app.jwtExpirationMs}")
    private long expirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImplement userDetails = (UserDetailsImplement) authentication.getPrincipal();

        // Extract roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        return Jwts.builder()
                .setSubject((userDetails.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .claim("roles", roles)  // Add roles as a claim
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public void validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
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
            return (List<String>) claims.get("roles");
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean hasAdminRole(String token) {
        List<String> roles = getRolesFromJwtToken(token);
        return roles.contains("ROLE_ADMIN");
    }



}
