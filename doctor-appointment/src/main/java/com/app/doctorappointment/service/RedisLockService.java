package com.app.doctorappointment.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisLockService {

    private static final Logger log = LoggerFactory.getLogger(RedisLockService.class);
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final Map<String, FallbackLock> fallbackLocks = new ConcurrentHashMap<>();

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean lock(String key, String value) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(key, value, LOCK_TTL));
        } catch (RuntimeException ex) {
            log.warn("Redis unavailable, using in-memory lock for {}: {}", key, ex.getMessage());
            Instant expiresAt = Instant.now().plus(LOCK_TTL);
            return fallbackLocks.compute(key, (ignored, current) -> {
                if (current == null || current.isExpired()) {
                    return new FallbackLock(value, expiresAt);
                }
                return current;
            }).matches(value);
        }
    }

    public void unlock(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Redis unavailable while releasing {}: {}", key, ex.getMessage());
        } finally {
            fallbackLocks.remove(key);
        }
    }

    public void unlockIfOwned(String key, String expectedValue) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (expectedValue != null && expectedValue.equals(currentValue)) {
                redisTemplate.delete(key);
            }
        } catch (RuntimeException ex) {
            log.warn("Redis unavailable while releasing owned {}: {}", key, ex.getMessage());
        } finally {
            fallbackLocks.computeIfPresent(key, (ignored, current) -> {
                if (current.isExpired() || current.matches(expectedValue)) {
                    return null;
                }
                return current;
            });
        }
    }

    public boolean isLockedBy(String key, String expectedValue) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            return expectedValue != null && expectedValue.equals(currentValue);
        } catch (RuntimeException ex) {
            log.warn("Redis unavailable while checking owner for {}: {}", key, ex.getMessage());
            FallbackLock lock = fallbackLocks.get(key);
            if (lock == null) {
                return false;
            }
            if (lock.isExpired()) {
                fallbackLocks.remove(key, lock);
                return false;
            }
            return lock.matches(expectedValue);
        }
    }

    private record FallbackLock(String value, Instant expiresAt) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        private boolean matches(String expectedValue) {
            return expectedValue != null && expectedValue.equals(value);
        }
    }
}
