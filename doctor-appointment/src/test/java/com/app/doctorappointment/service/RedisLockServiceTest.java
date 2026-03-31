package com.app.doctorappointment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisLockService redisLockService;

    @BeforeEach
    void setUp() {
        redisLockService = new RedisLockService(redisTemplate);
    }

    @Test
    void fallsBackToInMemoryLocksWhenRedisIsUnavailable() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new RedisConnectionFailureException("Redis is down"));
        doThrow(new RedisConnectionFailureException("Redis is down"))
                .when(redisTemplate).delete(anyString());

        assertTrue(redisLockService.lock("lock:slot:1", "abc123"));
        assertFalse(redisLockService.lock("lock:slot:1", "abc456"));

        redisLockService.unlock("lock:slot:1");

        assertTrue(redisLockService.lock("lock:slot:1", "abc789"));
    }
}
