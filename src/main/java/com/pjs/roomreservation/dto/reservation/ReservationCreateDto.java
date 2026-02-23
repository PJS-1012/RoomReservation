package com.pjs.roomreservation.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "회의실 예약 DTO")
public class ReservationCreateDto {

    @NotNull(message = "회의실 ID를 입력하세요.")
    @Schema(description = "해당하는 회의실 ID", example = "1")
    private Long roomId;

    @NotNull(message = "시작 시간을 입력하세요.")
    @Schema(description = "회의실 예약 시작 시간", example = "2026-03-01T14:30:00")
    private LocalDateTime startAt;

    @NotNull(message = "종료 시간을 입력하세요.")
    @Schema(description = "회의실 예약 종료 시간", example = "2026-03-01T15:30:00")
    private LocalDateTime endAt;

    public ReservationCreateDto() {}

    public Long getRoomId() {
        return roomId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
}
