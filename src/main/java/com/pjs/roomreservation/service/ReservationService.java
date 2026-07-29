package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Reservation;
import com.pjs.roomreservation.dto.PageResponseDto;
import com.pjs.roomreservation.dto.reservation.AdminReservationResponseDto;
import com.pjs.roomreservation.dto.reservation.ReservationResponseDto;
import com.pjs.roomreservation.global.cache.ReservationAvailabilityChangedEvent;
import com.pjs.roomreservation.repository.ReservationRepository;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.service.exception.ReservationConflictException;
import com.pjs.roomreservation.service.exception.ReservationForbiddenException;
import com.pjs.roomreservation.service.exception.ReservationNotFoundException;
import com.pjs.roomreservation.service.exception.RoomNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private static final int MAX_PAGE_SIZE = 100;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;
    private final ReservationTimeValidator reservationTimeValidator;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationService(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            UserService userService,
            ReservationTimeValidator reservationTimeValidator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userService = userService;
        this.reservationTimeValidator = reservationTimeValidator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long create(Long userId, Long roomId, LocalDateTime startAt, LocalDateTime endAt){
        var user = userService.getActiveById(userId);
        var room = roomRepository.findByIdForUpdate(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        reservationTimeValidator.validate(startAt, endAt);

        boolean overlap = reservationRepository.existsOverlapping(roomId, startAt, endAt);
        if(overlap){
            throw new ReservationConflictException();
        }

        Reservation reservation = new Reservation(user, room, startAt, endAt);
        reservationRepository.save(reservation);
        eventPublisher.publishEvent(new ReservationAvailabilityChangedEvent());

        return reservation.getId();
    }

    public PageResponseDto<ReservationResponseDto> showList(Long userId, int page, int size){
        userService.getActiveById(userId);

        Pageable pageable = createPageable(page, size);
        return PageResponseDto.from(
                reservationRepository.findReservationResponsesByUserId(userId, pageable)
        );
    }

    public PageResponseDto<AdminReservationResponseDto> showRoomReservationsForAdmin(
            Long roomId,
            int page,
            int size
    ) {
        roomRepository.findByIdAndActiveTrue(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        Pageable pageable = createPageable(page, size);
        return PageResponseDto.from(
                reservationRepository.findAdminReservationResponsesByRoomId(roomId, pageable)
        );
    }

    private Pageable createPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
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
            eventPublisher.publishEvent(new ReservationAvailabilityChangedEvent());
        }
    }

}
