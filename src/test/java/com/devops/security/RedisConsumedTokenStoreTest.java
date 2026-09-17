package com.devops.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisConsumedTokenStoreTest {

    @SuppressWarnings("unchecked")
    @Test
    void markConsumedIfAbsent_returnsTrueOnFirstUse() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("jti-1", "1", Duration.ofSeconds(30))).thenReturn(true);

        RedisConsumedTokenStore store = new RedisConsumedTokenStore(redisTemplate);

        assertThat(store.markConsumedIfAbsent("jti-1", Duration.ofSeconds(30))).isTrue();
        verify(valueOps).setIfAbsent("jti-1", "1", Duration.ofSeconds(30));
    }

    @SuppressWarnings("unchecked")
    @Test
    void markConsumedIfAbsent_returnsFalseWhenAlreadyConsumed() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("jti-1", "1", Duration.ofSeconds(30))).thenReturn(false);

        RedisConsumedTokenStore store = new RedisConsumedTokenStore(redisTemplate);

        assertThat(store.markConsumedIfAbsent("jti-1", Duration.ofSeconds(30))).isFalse();
    }
}
