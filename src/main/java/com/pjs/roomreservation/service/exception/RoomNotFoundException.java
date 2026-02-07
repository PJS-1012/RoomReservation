package com.pjs.roomreservation.service.exception;

public class RoomNotFoundException extends RuntimeException{
    public RoomNotFoundException(Long id){
        super("회의실을 찾을 수 없습니다. ID : " + id);
    }
}
