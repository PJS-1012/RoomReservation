package com.pjs.roomreservation.service;

import com.pjs.roomreservation.dto.room.RoomResponseDto;
import com.pjs.roomreservation.global.cache.CacheNames;
import com.pjs.roomreservation.repository.RoomRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomAvailabilityQueryService {
    private final RoomRepository roomRepository;

    public RoomAvailabilityQueryService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Cacheable(
            cacheNames = CacheNames.AVAILABLE_ROOMS,
            keyGenerator = "availableRoomKeyGenerator"
    )
    public List<RoomResponseDto> findAvailableRooms(
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer capacity
    ) {
        return roomRepository.findAvailableRoomResponses(startAt, endAt, capacity);
    }
}
