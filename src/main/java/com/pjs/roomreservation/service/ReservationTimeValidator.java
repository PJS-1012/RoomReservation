package com.pjs.roomreservation.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ReservationTimeValidator {
    private static final Duration MIN_RESERVATION_DURATION = Duration.ofMinutes(30);
    private static final Duration MAX_RESERVATION_DURATION = Duration.ofHours(4);

    public void validate(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("시작 시간과 종료 시간을 입력하세요.");
        }

        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
        }

        if (startAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("지난 시간은 예약할 수 없습니다.");
        }

        Duration duration = Duration.between(startAt, endAt);
        if (duration.compareTo(MIN_RESERVATION_DURATION) < 0) {
            throw new IllegalArgumentException("예약은 최소 30분 이상이어야 합니다.");
        }

        if (duration.compareTo(MAX_RESERVATION_DURATION) > 0) {
            throw new IllegalArgumentException("예약은 최대 4시간까지 가능합니다.");
        }
    }
}
