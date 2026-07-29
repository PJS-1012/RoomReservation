package com.pjs.roomreservation.service;

import com.pjs.roomreservation.dto.room.RoomResponseDto;
import com.pjs.roomreservation.global.cache.CacheNames;
import com.pjs.roomreservation.repository.RoomRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomQueryService {
    private final RoomRepository roomRepository;

    public RoomQueryService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Cacheable(cacheNames = CacheNames.ROOMS, key = "'active'")
    public List<RoomResponseDto> listActiveRooms() {
        return roomRepository.findAllByActiveTrueOrderByIdAsc().stream()
                .map(room -> new RoomResponseDto(
                        room.getId(),
                        room.getName(),
                        room.getLocation(),
                        room.getCapacity(),
                        room.isActive(),
                        room.getCreatedAt()
                ))
                .toList();
    }
}
