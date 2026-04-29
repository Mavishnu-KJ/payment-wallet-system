package com.example.walletservice.service;

public interface RedisLockService {
    boolean acquireLockWithTimeout(Long walletId, long timeoutSeconds);
    void releaseLock(Long walletId);
}
