package com.jeduardo.ticketing.billing.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockService implements LockService {
    private static final String LOCK_PREFIX = "lock:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean acquire(String key, long ttlSeconds) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX + key, "LOCKED", Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void release(String key) {
        redisTemplate.delete(LOCK_PREFIX + key);
        log.debug("Lock liberado: {}", LOCK_PREFIX + key);
    }
}