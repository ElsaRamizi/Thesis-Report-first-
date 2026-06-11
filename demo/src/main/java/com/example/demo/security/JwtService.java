package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
// handles creating and checking JWT tokens (used after login)
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /// turns the secret string from application.properties into a signing key
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /// builds a new JWT when user logs in — email is subject, role + tokenVersion are extra claims
    public String generateToken(String email, String role, long tokenVersion) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("tv", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /// reads email from token (the "sub" field)
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /// reads role claim — not really used for auth, we load role from DB in JwtFilter
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /// reads tokenVersion from JWT — must match users.token_version or token is dead (e.g. after logout)
    public Long extractTokenVersion(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tv", Long.class);
    }

    /// full check: right email, not expired, tokenVersion still valid
    public boolean isTokenValid(String token, String email, Long tokenVersion) {
        final String extractedEmail = extractEmail(token);
        Long tokenTv = extractTokenVersion(token);
        long expectedTv = tokenVersion == null ? 0L : tokenVersion;
        long actualTv = tokenTv == null ? 0L : tokenTv;
        return extractedEmail.equals(email)
            && !isTokenExpired(token)
            && actualTv == expectedTv;
    }

    /// checks exp claim against current time
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /// generic helper to pull one field out of the JWT payload
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /// parses and verifies signature — throws if token was tampered with
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
