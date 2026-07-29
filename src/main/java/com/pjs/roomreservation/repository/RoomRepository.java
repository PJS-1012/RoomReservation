package com.pjs.roomreservation.repository;

import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.dto.room.RoomResponseDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
    boolean existsByNameAndActiveTrue(String name);
    Optional<Room> findByIdAndActiveTrue(Long Id);
    List<Room> findAllByActiveTrueOrderByIdAsc();

    @Query("""
            select new com.pjs.roomreservation.dto.room.RoomResponseDto(
                room.id,
                room.name,
                room.location,
                room.capacity,
                room.active,
                room.createdAt
            )
            from Room room
            where room.active = true
              and (:capacity is null or room.capacity >= :capacity)
              and not exists (
                  select reservation.id
                  from Reservation reservation
                  where reservation.room.id = room.id
                    and reservation.canceled = false
                    and reservation.startAt < :endAt
                    and reservation.endAt > :startAt
              )
            order by room.id asc
            """)
    List<RoomResponseDto> findAvailableRoomResponses(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("capacity") Integer capacity
    );
}
