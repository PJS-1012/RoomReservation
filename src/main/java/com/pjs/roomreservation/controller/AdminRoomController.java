package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.reservation.AdminReservationResponseDto;
import com.pjs.roomreservation.dto.room.RoomCreateDto;
import com.pjs.roomreservation.dto.room.RoomUpdateDto;
import com.pjs.roomreservation.service.ReservationService;
import com.pjs.roomreservation.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Room Management", description = "관리자 권한 기반 회의실 관리 기능")
public class AdminRoomController {
    private final RoomService roomService;
    private final ReservationService reservationService;

    public AdminRoomController(RoomService roomService, ReservationService reservationService) {
        this.roomService = roomService;
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회의실 생성")
    public Long create(@Valid @RequestBody RoomCreateDto req) {
        return roomService.create(req.getName(), req.getLocation(), req.getCapacity());
    }

    @PutMapping("{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "회의실 갱신")
    public void update(@PathVariable Long roomId, @Valid @RequestBody RoomUpdateDto req){
        roomService.update(roomId, req.getName(), req.getLocation(), req.getCapacity());
    }

    @DeleteMapping("{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "회의실 삭제(비활성화)")
    public void deactivate(@PathVariable Long roomId){
        roomService.deactivate(roomId);
    }

    @GetMapping("/rooms/{roomId}/reservations")
    @Operation(summary = "회의실 별 예약 확인")
    public List<AdminReservationResponseDto> reservations(@PathVariable Long roomId) {
        return reservationService.showRoomReservationsForAdmin(roomId).stream()
                .map(reservation -> new AdminReservationResponseDto(
                        reservation.getId(),
                        reservation.getRoom().getId(),
                        reservation.getRoom().getName(),
                        reservation.getUser().getId(),
                        reservation.getUser().getName(),
                        reservation.getUser().getEmail(),
                        reservation.getStartAt(),
                        reservation.getEndAt(),
                        reservation.isCanceled(),
                        reservation.getCreatedAt()
                ))
                .toList();
    }
}
