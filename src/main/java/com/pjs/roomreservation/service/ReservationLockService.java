package com.pjs.roomreservation.service;

import com.pjs.roomreservation.config.ReservationLockProperties;
import com.pjs.roomreservation.service.exception.ReservationLockException;
import io.micrometer.core.instrument.MeterRegistry;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ReservationLockService {
    private static final Logger log = LoggerFactory.getLogger(ReservationLockService.class);
    private static final String LOCK_KEY_PREFIX = "reservation:room:";

    private final ReservationService reservationService;
    private final Optional<RedissonClient> redissonClient;
    private final ReservationLockProperties lockProperties;
    private final MeterRegistry meterRegistry;

    public ReservationLockService(
            ReservationService reservationService,
            Optional<RedissonClient> redissonClient,
            ReservationLockProperties lockProperties,
            MeterRegistry meterRegistry
    ) {
        this.reservationService = reservationService;
        this.redissonClient = redissonClient;
        this.lockProperties = lockProperties;
        this.meterRegistry = meterRegistry;
    }

    public Long create(Long userId, Long roomId, LocalDateTime startAt, LocalDateTime endAt) {
        if (!lockProperties.isEnabled() || redissonClient.isEmpty()) {
            recordLockOutcome("disabled");
            return reservationService.create(userId, roomId, startAt, endAt);
        }

        RLock lock;
        try {
            lock = redissonClient.get().getLock(LOCK_KEY_PREFIX + roomId);
            boolean acquired = lock.tryLock(lockProperties.getWaitTime().toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                recordLockOutcome("not_acquired");
                throw new ReservationLockException();
            }
            recordLockOutcome("acquired");
        } catch (RedisException e) {
            recordLockOutcome("redis_error_fallback");
            log.warn("Redis lock unavailable. Falling back to database lock. roomId={}", roomId, e);
            return reservationService.create(userId, roomId, startAt, endAt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            recordLockOutcome("interrupted");
            throw new ReservationLockException();
        }

        try {
            return reservationService.create(userId, roomId, startAt, endAt);
        } finally {
            unlockSafely(lock, roomId);
        }
    }

    private void unlockSafely(RLock lock, Long roomId) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (RedisException e) {
            log.error("Failed to release Redis reservation lock. roomId={}", roomId, e);
        }
    }

    private void recordLockOutcome(String outcome) {
        meterRegistry.counter("reservation.lock.requests", "outcome", outcome).increment();
    }
}
