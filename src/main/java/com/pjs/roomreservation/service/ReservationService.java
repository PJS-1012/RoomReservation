package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Reservation;
import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.ReservationRepository;
import com.pjs.roomreservation.service.exception.ReservationConflictException;
import com.pjs.roomreservation.service.exception.ReservationForbiddenException;
import com.pjs.roomreservation.service.exception.ReservationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomService roomService;
    private final UserService userService;

    public ReservationService(ReservationRepository reservationRepository, RoomService roomService, UserService userService) {
        this.reservationRepository = reservationRepository;
        this.roomService = roomService;
        this.userService = userService;
    }

    @Transactional
    public Long create(Long userId, Long roomId, LocalDateTime startAt, LocalDateTime endAt){
        var user = userService.getActiveById(userId);
        var room = roomService.getActiveById(roomId);

        if(!startAt.isBefore(endAt)){
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
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
