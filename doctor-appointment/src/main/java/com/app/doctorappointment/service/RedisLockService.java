package com.app.doctorappointment.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisLockService {

    private static final Logger log = LoggerFactory.getLogger(RedisLockService.class);

    private final StringRedisTemplate redisTemplate;
    private final Set<String> fallbackLocks = ConcurrentHashMap.newKeySet();

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean lock(String key, String value) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMinutes(5)));
        } catch (RuntimeException ex) {
            log.warn("Redis unavailable, using in-memory lock for {}: {}", key, ex.getMessage());
            return fallbackLocks.add(key);
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
}
