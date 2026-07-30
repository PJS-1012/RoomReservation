package com.pjs.roomreservation.service;

import com.pjs.roomreservation.config.ReservationLockProperties;
import com.pjs.roomreservation.service.exception.ReservationLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationLockServiceTest {
    private static final Long USER_ID = 1L;
    private static final Long ROOM_ID = 2L;
    private static final LocalDateTime START_AT = LocalDateTime.of(2030, 1, 1, 10, 0);
    private static final LocalDateTime END_AT = LocalDateTime.of(2030, 1, 1, 11, 0);

    @Mock ReservationService reservationService;
    @Mock RedissonClient redissonClient;
    @Mock RLock lock;

    @Test
    void create_whenLockIsDisabled_usesDatabaseLockPath() {
        ReservationLockService service = service(false, Optional.of(redissonClient));

        service.create(USER_ID, ROOM_ID, START_AT, END_AT);

        verify(reservationService).create(USER_ID, ROOM_ID, START_AT, END_AT);
        verify(redissonClient, never()).getLock("reservation:room:" + ROOM_ID);
    }

    @Test
    void create_whenLockIsAcquired_createsReservationAndReleasesLock() throws Exception {
        ReservationLockService service = service(true, Optional.of(redissonClient));
        when(redissonClient.getLock("reservation:room:" + ROOM_ID)).thenReturn(lock);
        when(lock.tryLock(0, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        service.create(USER_ID, ROOM_ID, START_AT, END_AT);

        verify(reservationService).create(USER_ID, ROOM_ID, START_AT, END_AT);
        verify(lock).unlock();
    }

    @Test
    void create_whenLockIsUnavailable_throwsConflict() throws Exception {
        ReservationLockService service = service(true, Optional.of(redissonClient));
        when(redissonClient.getLock("reservation:room:" + ROOM_ID)).thenReturn(lock);
        when(lock.tryLock(0, TimeUnit.MILLISECONDS)).thenReturn(false);

        assertThatThrownBy(() -> service.create(USER_ID, ROOM_ID, START_AT, END_AT))
                .isInstanceOf(ReservationLockException.class);

        verify(reservationService, never()).create(USER_ID, ROOM_ID, START_AT, END_AT);
    }

    @Test
    void create_whenRedisFails_fallsBackToDatabaseLockPath() {
        ReservationLockService service = service(true, Optional.of(redissonClient));
        doThrow(new RedisException("Redis unavailable"))
                .when(redissonClient).getLock("reservation:room:" + ROOM_ID);

        service.create(USER_ID, ROOM_ID, START_AT, END_AT);

        verify(reservationService).create(USER_ID, ROOM_ID, START_AT, END_AT);
    }

    private ReservationLockService service(boolean enabled, Optional<RedissonClient> client) {
        ReservationLockProperties properties = new ReservationLockProperties();
        properties.setEnabled(enabled);
        properties.setWaitTime(Duration.ZERO);
        return new ReservationLockService(reservationService, client, properties);
    }
}
