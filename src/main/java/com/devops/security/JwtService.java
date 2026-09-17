package com.devops.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key signingKey;
    private final long defaultTtlSeconds;
    private final ConsumedTokenStore consumedTokenStore;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.default-ttl-seconds:60}") long defaultTtlSeconds,
            ConsumedTokenStore consumedTokenStore) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.defaultTtlSeconds = defaultTtlSeconds;
        this.consumedTokenStore = consumedTokenStore;
    }

    public String issueToken(long ttlSeconds) {
        long effectiveTtl = ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds;
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(effectiveTtl)))
                .signWith(signingKey)
                .compact();
    }

    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    public void validateAndConsume(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtValidationException("Missing JWT");
        }
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid JWT: " + ex.getMessage());
        }

        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            throw new JwtValidationException("JWT missing jti claim");
        }

        boolean firstUse = consumedTokenStore.markConsumedIfAbsent(jti, remainingTtl(claims.getExpiration()));
        if (!firstUse) {
            throw new JwtValidationException("JWT already used for a previous transaction");
        }
    }

    private Duration remainingTtl(Date expiration) {
        long secondsLeft = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return Duration.ofSeconds(Math.max(1, secondsLeft));
    }

    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String message) {
            super(message);
        }
    }
}
