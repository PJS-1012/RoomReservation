package com.pjs.roomreservation.repository;

import com.pjs.roomreservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository {
    @Query("select count(r) > 0 from Reservation r where r.room.id = :roomId and r.canceled = false and r.startAt < :endAt and r.endAt >: startAt")
    boolean existsOverlapping (@Param("roomId") Long roomId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    List<Reservation> findAllByUserIdOrderByStartAtDesc(Long userId);
}
