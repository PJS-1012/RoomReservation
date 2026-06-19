package com.pjs.roomreservation.repository;

import com.pjs.roomreservation.domain.Reservation;
import com.pjs.roomreservation.dto.reservation.AdminReservationResponseDto;
import com.pjs.roomreservation.dto.reservation.ReservationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    //Optional<Reservation> findById(Long Id);
    @Query("select count(r) > 0 from Reservation r where r.room.id = :roomId and r.canceled = false and r.startAt < :endAt and r.endAt > :startAt")
    boolean existsOverlapping (@Param("roomId") Long roomId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Query(value = """
            select new com.pjs.roomreservation.dto.reservation.ReservationResponseDto(
                r.id,
                room.id,
                room.name,
                r.startAt,
                r.endAt,
                r.canceled,
                r.createdAt
            )
            from Reservation r
            join r.room room
            where r.user.id = :userId
            order by r.startAt desc
            """,
            countQuery = """
                    select count(r)
                    from Reservation r
                    where r.user.id = :userId
                    """)
    Page<ReservationResponseDto> findReservationResponsesByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(value = """
            select new com.pjs.roomreservation.dto.reservation.AdminReservationResponseDto(
                r.id,
                room.id,
                room.name,
                user.id,
                user.name,
                user.email,
                r.startAt,
                r.endAt,
                r.canceled,
                r.createdAt
            )
            from Reservation r
            join r.room room
            join r.user user
            where room.id = :roomId
            order by r.createdAt desc, r.id desc
            """,
            countQuery = """
                    select count(r)
                    from Reservation r
                    where r.room.id = :roomId
                    """)
    Page<AdminReservationResponseDto> findAdminReservationResponsesByRoomId(
            @Param("roomId") Long roomId,
            Pageable pageable
    );
}
