package com.pjs.roomreservation.service;

import com.pjs.roomreservation.dto.room.RoomResponseDto;
import com.pjs.roomreservation.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomAvailabilityService {
    private final RoomRepository roomRepository;
    private final ReservationTimeValidator reservationTimeValidator;

    public RoomAvailabilityService(
            RoomRepository roomRepository,
            ReservationTimeValidator reservationTimeValidator
    ) {
        this.roomRepository = roomRepository;
        this.reservationTimeValidator = reservationTimeValidator;
    }

    public List<RoomResponseDto> findAvailableRooms(
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer capacity
    ) {
        reservationTimeValidator.validate(startAt, endAt);

        if (capacity != null && capacity < 1) {
            throw new IllegalArgumentException("수용 인원은 1명 이상이어야 합니다.");
        }

        return roomRepository.findAvailableRoomResponses(startAt, endAt, capacity);
    }
}
