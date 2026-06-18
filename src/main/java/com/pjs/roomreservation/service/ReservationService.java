package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Reservation;
import com.pjs.roomreservation.repository.ReservationRepository;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.service.exception.ReservationConflictException;
import com.pjs.roomreservation.service.exception.ReservationForbiddenException;
import com.pjs.roomreservation.service.exception.ReservationNotFoundException;
import com.pjs.roomreservation.service.exception.RoomNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private static final Duration MIN_RESERVATION_DURATION = Duration.ofMinutes(30);
    private static final Duration MAX_RESERVATION_DURATION = Duration.ofHours(4);
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;

    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository, UserService userService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userService = userService;
    }

    @Transactional
    public Long create(Long userId, Long roomId, LocalDateTime startAt, LocalDateTime endAt){
        var user = userService.getActiveById(userId);
        var room = roomRepository.findByIdForUpdate(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        if(!startAt.isBefore(endAt)){
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
        }

        if(startAt.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("지난 시간은 예약할 수 없습니다.");
        }

        Duration duration = Duration.between(startAt, endAt);
        if(duration.compareTo(MIN_RESERVATION_DURATION) < 0){
            throw new IllegalArgumentException("예약은 최소 30분 이상이어야 합니다.");
        }

        if(duration.compareTo(MAX_RESERVATION_DURATION) > 0){
            throw new IllegalArgumentException("예약은 최대 4시간까지 가능합니다.");
        }

        boolean overlap = reservationRepository.existsOverlapping(roomId, startAt, endAt);
        if(overlap){
            throw new ReservationConflictException();
        }

        Reservation reservation = new Reservation(user, room, startAt, endAt);
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    public List<Reservation> showList(Long userId){
        userService.getActiveById(userId);

        return reservationRepository.findAllByUserIdOrderByStartAtDesc(userId);
    }

    public List<Reservation> showRoomReservationsForAdmin(Long roomId) {
        roomRepository.findByIdAndActiveTrue(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        return reservationRepository.findAllByRoomIdOrderByCreatedAtDescIdDesc(roomId);
    }

    @Transactional
    public void cancel(Long userId, Long reservationId){
        userService.getActiveById(userId);

        Reservation r = reservationRepository.findById(reservationId).orElseThrow(() -> new ReservationNotFoundException(reservationId));;

        if(!r.getUser().getId().equals(userId)){
            throw new ReservationForbiddenException();
        }

        if(!r.isCanceled()){
            r.cancel();
        }
    }

}
