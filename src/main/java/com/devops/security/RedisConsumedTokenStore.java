package com.devops.security;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisConsumedTokenStore implements ConsumedTokenStore {

    private static final String CONSUMED_VALUE = "1";

    private final StringRedisTemplate redisTemplate;

    public RedisConsumedTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean markConsumedIfAbsent(String key, Duration ttl) {
        Boolean firstUse = redisTemplate.opsForValue().setIfAbsent(key, CONSUMED_VALUE, ttl);
        return Boolean.TRUE.equals(firstUse);
    }
}
