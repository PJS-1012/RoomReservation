package com.pjs.roomreservation.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "예약 가능 회의실 조회 조건 DTO")
public class AvailableRoomSearchDto {
    @NotNull(message = "시작 시간을 입력하세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "회의실 예약 시작 시간", example = "2026-08-01T14:00:00")
    private LocalDateTime startAt;

    @NotNull(message = "종료 시간을 입력하세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "회의실 예약 종료 시간", example = "2026-08-01T15:00:00")
    private LocalDateTime endAt;

    @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다.")
    @Schema(description = "최소 회의실 수용 인원", example = "4")
    private Integer capacity;

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
