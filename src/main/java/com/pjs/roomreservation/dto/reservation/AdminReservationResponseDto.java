package com.pjs.roomreservation.dto.reservation;

import java.time.LocalDateTime;

public class AdminReservationResponseDto {
    private final Long reservationId;
    private final Long roomId;
    private final String roomName;
    private final Long userId;
    private final String userName;
    private final String userEmail;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final Boolean canceled;
    private final LocalDateTime createdAt;

    public AdminReservationResponseDto(
            Long reservationId,
            Long roomId,
            String roomName,
            Long userId,
            String userName,
            String userEmail,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Boolean canceled,
            LocalDateTime createdAt
    ) {
        this.reservationId = reservationId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.startAt = startAt;
        this.endAt = endAt;
        this.canceled = canceled;
        this.createdAt = createdAt;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
