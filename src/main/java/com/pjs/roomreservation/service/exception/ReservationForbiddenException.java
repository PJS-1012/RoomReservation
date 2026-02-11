package com.pjs.roomreservation.service.exception;

public class ReservationForbiddenException extends RuntimeException{
    public ReservationForbiddenException(){
        super("해당 예약에 대한 권한이 없습니다.");
    }
}
