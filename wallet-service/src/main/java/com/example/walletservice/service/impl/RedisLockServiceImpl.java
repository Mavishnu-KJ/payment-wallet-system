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
    private static final long LOCK_TIMEOUT_SECONDS = 120;

    public boolean acquireLock(Long walletId) {
        logger.info("acquireLock, walletId is {}", walletId);

        String lockKey = LOCK_PREFIX + walletId;
        logger.info("acquireLock, lockKey is {}", lockKey);

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        logger.info("acquireLock, acquired is {}", acquired);

        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(Long walletId) {
        logger.info("releaseLock, walletId is {}", walletId);

        String lockKey = LOCK_PREFIX + walletId;
        logger.info("releaseLock, lockKey is {}", lockKey);

        redisTemplate.delete(lockKey);
    }

}
