package com.jeduardo.ticketing.billing.lock;

import com.jeduardo.ticketing.billing.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockExecutor {

    private final LockService lockService;

    public <T> T executeWithLock(String key, long ttlSeconds, Supplier<T> action) {
        boolean acquired = lockService.acquire(key, ttlSeconds);

        if (!acquired) {
            log.warn("Lock indisponivel para chave: {}", key);
            throw new NegocioException("Geracao de cobranca em andamento.");
        }

        try {
            return action.get();
        } finally {
            lockService.release(key);
        }
    }
}