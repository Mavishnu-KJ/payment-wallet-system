package com.example.walletservice.service.impl;

import com.example.walletservice.service.RedisLockService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisLockServiceImpl implements RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RedisLockServiceImpl.class);

    private static final String LOCK_PREFIX = "lock:wallet:";

    public boolean acquireLockWithTimeout(Long walletId, long timeoutSeconds) {
        logger.info("acquireLockWithTimeout, walletId is {}", walletId);

        String lockKey = LOCK_PREFIX + walletId;
        logger.info("acquireLockWithTimeout, lockKey is {}", lockKey);

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", timeoutSeconds, TimeUnit.SECONDS);
        logger.info("acquireLockWithTimeout, acquired is {}", acquired);

        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(Long walletId) {
        logger.info("releaseLock, walletId is {}", walletId);

        String lockKey = LOCK_PREFIX + walletId;
        logger.info("releaseLock, lockKey is {}", lockKey);

        redisTemplate.delete(lockKey);
    }

}
