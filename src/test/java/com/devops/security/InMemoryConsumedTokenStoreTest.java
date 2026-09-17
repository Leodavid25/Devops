package com.devops.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class InMemoryConsumedTokenStoreTest {

    @Test
    void markConsumedIfAbsent_firstCallReturnsTrue() {
        InMemoryConsumedTokenStore store = new InMemoryConsumedTokenStore();

        assertThat(store.markConsumedIfAbsent("key-1", Duration.ofSeconds(30))).isTrue();
    }

    @Test
    void markConsumedIfAbsent_secondCallReturnsFalse() {
        InMemoryConsumedTokenStore store = new InMemoryConsumedTokenStore();
        store.markConsumedIfAbsent("key-1", Duration.ofSeconds(30));

        assertThat(store.markConsumedIfAbsent("key-1", Duration.ofSeconds(30))).isFalse();
    }

    @Test
    void markConsumedIfAbsent_allowsReuseOfKeyAfterItExpires() throws InterruptedException {
        InMemoryConsumedTokenStore store = new InMemoryConsumedTokenStore();
        store.markConsumedIfAbsent("key-1", Duration.ofMillis(50));

        Thread.sleep(100);

        assertThat(store.markConsumedIfAbsent("key-1", Duration.ofSeconds(30))).isTrue();
    }
}
