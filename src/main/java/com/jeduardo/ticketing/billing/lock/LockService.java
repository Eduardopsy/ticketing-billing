package com.jeduardo.ticketing.billing.lock;

public interface LockService {
    boolean acquire(String key, long ttlSeconds);
    void release(String key);
}