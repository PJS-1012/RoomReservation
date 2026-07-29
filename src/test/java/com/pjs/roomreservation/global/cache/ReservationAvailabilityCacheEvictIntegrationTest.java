package com.pjs.roomreservation.global.cache;

import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.ReservationRepository;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.repository.UserRepository;
import com.pjs.roomreservation.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReservationAvailabilityCacheEvictIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void clearCache() {
        cacheManager.getCache(CacheNames.AVAILABLE_ROOMS).clear();
        reservationRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_evictsAvailableRoomCache_afterTransactionCommit() {
        Cache cache = cacheManager.getCache(CacheNames.AVAILABLE_ROOMS);
        cache.put("stale-key", "stale-value");

        Long userId = userRepository.save(new User("reservation-cache-create@test.com", "password", "tester")).getId();
        Long roomId = roomRepository.save(new Room("Reservation Cache Create Room", "3F", 4)).getId();
        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);

        reservationService.create(userId, roomId, startAt, startAt.plusHours(1));

        assertThat(cache.get("stale-key")).isNull();
    }

    @Test
    void cancel_evictsAvailableRoomCache_afterTransactionCommit() {
        Cache cache = cacheManager.getCache(CacheNames.AVAILABLE_ROOMS);
        Long userId = userRepository.save(new User("reservation-cache-cancel@test.com", "password", "tester")).getId();
        Long roomId = roomRepository.save(new Room("Reservation Cache Cancel Room", "3F", 4)).getId();
        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        Long reservationId = reservationService.create(userId, roomId, startAt, startAt.plusHours(1));
        cache.put("stale-key", "stale-value");

        reservationService.cancel(userId, reservationId);

        assertThat(cache.get("stale-key")).isNull();
    }
}
