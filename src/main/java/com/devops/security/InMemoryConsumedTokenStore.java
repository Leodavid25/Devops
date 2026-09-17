package com.devops.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryConsumedTokenStore implements ConsumedTokenStore {

    private final ConcurrentHashMap<String, Instant> consumed = new ConcurrentHashMap<>();

    @Override
    public boolean markConsumedIfAbsent(String key, Duration ttl) {
        purgeExpired();
        Instant expiresAt = Instant.now().plus(ttl);
        return consumed.putIfAbsent(key, expiresAt) == null;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        consumed.values().removeIf(expiresAt -> expiresAt.isBefore(now));
    }
}
