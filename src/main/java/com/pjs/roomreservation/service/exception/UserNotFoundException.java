package com.pjs.roomreservation.service.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String email){
        super("사용자를 찾을 수 없습니다. Email : " + email);
    }
    public UserNotFoundException(Long userId){
        super("사용자를 찾을 수 없습니다. ID : " + userId);
    }
}
