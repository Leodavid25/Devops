package com.devops.security;

import java.time.Duration;


public interface ConsumedTokenStore {

    boolean markConsumedIfAbsent(String key, Duration ttl);
}
