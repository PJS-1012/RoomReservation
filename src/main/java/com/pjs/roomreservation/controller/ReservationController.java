package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.reservation.ReservationCreateDto;
import com.pjs.roomreservation.dto.reservation.ReservationResponseDto;
import com.pjs.roomreservation.security.UserPrincipal;
import com.pjs.roomreservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservation Controller", description = "각종 회의실 예약 관련 기능")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "원하는 시간대 직접 입력해 회의실 예약")
    public Long create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ReservationCreateDto req){
        return reservationService.create(principal.getUserId(), req.getRoomId(), req.getStartAt(), req.getEndAt());
    }

    @GetMapping
    @Operation(summary = "본인이 예약한 회의실 정보 확인")
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
    @Operation(summary = "본인이 예약한 회의실 취소")
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long reservationId){
        reservationService.cancel(principal.getUserId(), reservationId);
    }
}
