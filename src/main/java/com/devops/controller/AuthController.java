package com.devops.controller;

import com.devops.dto.TokenResponse;
import com.devops.security.ApiKeyAuthFilter;
import com.devops.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthController {

    private final JwtService jwtService;
    private final String expectedApiKey;

    public AuthController(JwtService jwtService, @Value("${security.api-key}") String expectedApiKey) {
        this.jwtService = jwtService;
        this.expectedApiKey = expectedApiKey;
    }

    @PostMapping("/auth/token")
    public ResponseEntity<?> issueToken(
            @RequestHeader(value = ApiKeyAuthFilter.API_KEY_HEADER, required = false) String apiKey,
            @RequestParam(value = "ttlSeconds", required = false, defaultValue = "0") long ttlSeconds) {

        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ERROR");
        }

        String token = jwtService.issueToken(ttlSeconds);
        long effectiveTtl = ttlSeconds > 0 ? ttlSeconds : jwtService.getDefaultTtlSeconds();
        return ResponseEntity.ok(new TokenResponse(token, effectiveTtl));
    }
}
