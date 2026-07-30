package com.pjs.roomreservation.service.exception;

public class ReservationLockException extends RuntimeException {
    public ReservationLockException() {
        super("동일 회의실의 예약 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
    }
}
