package com.devops.config;

import com.devops.security.ConsumedTokenStore;
import com.devops.security.InMemoryConsumedTokenStore;
import com.devops.security.RedisConsumedTokenStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class TokenStoreConfig {

    @Bean
    @Profile("test")
    public ConsumedTokenStore inMemoryConsumedTokenStore() {
        return new InMemoryConsumedTokenStore();
    }

    @Bean
    @Profile("!test")
    public ConsumedTokenStore redisConsumedTokenStore(StringRedisTemplate redisTemplate) {
        return new RedisConsumedTokenStore(redisTemplate);
    }
}
