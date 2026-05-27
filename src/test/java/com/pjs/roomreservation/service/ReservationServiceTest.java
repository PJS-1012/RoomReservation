package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Long userId;
    private Long roomId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("reservation-test@test.com", "1234", "test"));
        Room room = roomRepository.save(new Room("Reservation Test Room", "3F", 6));

        userId = user.getId();
        roomId = room.getId();
    }

    @Test
    void create_pastStartAt_throws() {
        LocalDateTime startAt = LocalDateTime.now().minusHours(1);
        LocalDateTime endAt = LocalDateTime.now().plusHours(1);

        assertThatThrownBy(() -> reservationService.create(userId, roomId, startAt, endAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지난 시간은 예약할 수 없습니다.");
    }

    @Test
    void create_lessThanThirtyMinutes_throws() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = startAt.plusMinutes(29);

        assertThatThrownBy(() -> reservationService.create(userId, roomId, startAt, endAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약은 최소 30분 이상이어야 합니다.");
    }

    @Test
    void create_moreThanFourHours_throws() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = startAt.plusHours(4).plusMinutes(1);

        assertThatThrownBy(() -> reservationService.create(userId, roomId, startAt, endAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약은 최대 4시간까지 가능합니다.");
    }
}
