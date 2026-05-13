package com.codesfree.prueba.security;

import com.codesfree.prueba.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${app.security.jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(AppUser user) {
        return buildToken(
                Map.of(
                        "role", user.getRole().name(),
                        "authorities", List.of(user.getRole().name()),
                        "tokenType", "access"),
                user.getUsername(),
                accessTokenExpiration);
    }

    public String generateRefreshToken(AppUser user) {
        return buildToken(Map.of("tokenType", "refresh"), user.getUsername(), refreshTokenExpiration);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractAllClaims(token).get("tokenType", String.class));
    }

    public boolean isRefreshTokenValid(String token, AppUser user) {
        return user.getUsername().equals(extractUsername(token)) && !isTokenExpired(token) && isRefreshToken(token);
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    public Instant getRefreshTokenExpiryInstant() {
        return Instant.now().plusMillis(refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMillis) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKey)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
