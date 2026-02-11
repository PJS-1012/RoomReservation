package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.reservation.ReservationCreateDto;
import com.pjs.roomreservation.dto.reservation.ReservationResponseDto;
import com.pjs.roomreservation.security.UserPrincipal;
import com.pjs.roomreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ReservationCreateDto req){
        return reservationService.create(principal.getUserId(), req.getRoomId(), req.getStartAt(), req.getEndAt());
    }

    @GetMapping
    public List<ReservationResponseDto> myReservation(@AuthenticationPrincipal UserPrincipal principal){
        return reservationService.showList(principal.getUserId()).stream()
                .map(r -> new ReservationResponseDto(
                        r.getId(),
                        r.getRoom().getId(),
                        r.getRoom().getName(),
                        r.getStartAt(),
                        r.getEndAt(),
                        r.isCanceled(),
                        r.getCreatedAt()))
                .toList();
    }

    @DeleteMapping("/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long reservationId){
        reservationService.cancel(principal.getUserId(), reservationId);
    }
}
