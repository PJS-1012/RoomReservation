package com.pjs.roomreservation.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "회의실 예약 정보 DTO")

public class ReservationResponseDto {

    @Schema(description = "")
    private final Long id;
    private final Long roomId;
    private final String roomName;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final boolean canceled;
    private final LocalDateTime createdAt;

    public ReservationResponseDto(Long id, Long roomId, String roomName, LocalDateTime startAt, LocalDateTime endAt, boolean canceled, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.roomName = roomName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.canceled = canceled;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
