package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Reservation;
import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.dto.room.RoomResponseDto;
import com.pjs.roomreservation.repository.ReservationRepository;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomAvailabilityServiceTest {

    @Autowired
    private RoomAvailabilityService roomAvailabilityService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAvailableRooms_excludesOverlappingAndInsufficientRooms() {
        User user = userRepository.save(new User("availability@test.com", "password", "tester"));
        Room availableRoom = roomRepository.save(new Room("Available Room", "3F", 8));
        Room occupiedRoom = roomRepository.save(new Room("Occupied Room", "3F", 8));
        roomRepository.save(new Room("Small Room", "3F", 2));

        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endAt = startAt.plusHours(1);
        reservationRepository.save(new Reservation(user, occupiedRoom, startAt.plusMinutes(30), endAt.plusMinutes(30)));

        List<RoomResponseDto> result = roomAvailabilityService.findAvailableRooms(startAt, endAt, 4);

        assertThat(result)
                .extracting(RoomResponseDto::getId)
                .contains(availableRoom.getId())
                .doesNotContain(occupiedRoom.getId());
        assertThat(result)
                .allMatch(room -> room.getCapacity() >= 4);
    }

    @Test
    void findAvailableRooms_allowsReservationStartingWhenPreviousReservationEnds() {
        User user = userRepository.save(new User("boundary@test.com", "password", "tester"));
        Room room = roomRepository.save(new Room("Boundary Room", "3F", 4));

        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        reservationRepository.save(new Reservation(user, room, startAt.minusHours(1), startAt));

        List<RoomResponseDto> result = roomAvailabilityService.findAvailableRooms(
                startAt,
                startAt.plusHours(1),
                null
        );

        assertThat(result).extracting(RoomResponseDto::getId).contains(room.getId());
    }
}
