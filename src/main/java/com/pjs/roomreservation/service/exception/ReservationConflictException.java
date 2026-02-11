package com.pjs.roomreservation.service.exception;

public class ReservationConflictException extends RuntimeException{
    public ReservationConflictException(){
        super("해당 시간대에 예약이 이미 존재합니다.");
    }
}
