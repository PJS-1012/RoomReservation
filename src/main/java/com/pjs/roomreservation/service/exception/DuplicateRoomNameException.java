package com.pjs.roomreservation.service.exception;

public class DuplicateRoomNameException extends RuntimeException{
    public DuplicateRoomNameException(String name){
        super("이미 존재하는 회의실 이름입니다 : " + name);
    }
}
