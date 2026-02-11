package com.pjs.roomreservation.service.exception;

public class ReservationNotFoundException extends RuntimeException{
    public ReservationNotFoundException(Long reservationId){
        super("예약을 찾을 수 없습니다. ID : " + reservationId);
    }
}
