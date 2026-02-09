package com.pjs.roomreservation.dto.reservation;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class ReservationCreateDto {

    @NotBlank(message = "회의실 ID를 입력하세요.")
    private Long roomId;

    @NotBlank(message = "시작 시간을 입력하세요.")
    private LocalDateTime startAt;

    @NotBlank(message = "종료 시간을 입력하세요.")
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
