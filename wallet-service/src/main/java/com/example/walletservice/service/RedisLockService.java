package com.example.walletservice.service;

public interface RedisLockService {
    boolean acquireLock(Long walletId);
    void releaseLock(Long walletId);
}
