package com.votersystem.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // Generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    // Generate token with additional claims
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        return createToken(claims, userDetails.getUsername());
    }
    
    // Create token with claims and subject
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    // Validate token without UserDetails
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // Extract role from token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    // Extract user type from token
    public String extractUserType(String token) {
        return extractClaim(token, claims -> claims.get("userType", String.class));
    }
    
    // Extract user ID from token
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }
    
    // Generate token for agent
    public String generateAgentToken(String username, String agentId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", agentId);
        claims.put("role", role);
        claims.put("userType", "AGENT");
        return createToken(claims, username);
    }

    // Generate token for administrator
    public String generateAdminToken(String username, String adminId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", adminId);
        claims.put("role", role);
        claims.put("userType", role.equals("MASTER") ? "MASTER" : "ADMIN");
        return createToken(claims, username);
    }
    
    // Get token expiration time in milliseconds
    public Long getExpirationTime() {
        return expiration;
    }
    
    // Check if token will expire soon (within 1 hour)
    public Boolean isTokenExpiringSoon(String token) {
        Date expiration = extractExpiration(token);
        Date oneHourFromNow = new Date(System.currentTimeMillis() + 3600000); // 1 hour
        return expiration.before(oneHourFromNow);
    }
    
    // Refresh token if it's expiring soon
    public String refreshTokenIfNeeded(String token, UserDetails userDetails) {
        if (isTokenExpiringSoon(token) && validateToken(token, userDetails)) {
            return generateToken(userDetails);
        }
        return token;
    }
}
