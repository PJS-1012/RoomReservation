package com.pjs.roomreservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.pjs.roomreservation.dto.room.RoomResponseDto;

@Service
@Transactional(readOnly = true)
public class RoomAvailabilityService {
    private final ReservationTimeValidator reservationTimeValidator;
    private final RoomAvailabilityQueryService roomAvailabilityQueryService;

    public RoomAvailabilityService(
            ReservationTimeValidator reservationTimeValidator,
            RoomAvailabilityQueryService roomAvailabilityQueryService
    ) {
        this.reservationTimeValidator = reservationTimeValidator;
        this.roomAvailabilityQueryService = roomAvailabilityQueryService;
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

        return roomAvailabilityQueryService.findAvailableRooms(startAt, endAt, capacity);
    }
}
