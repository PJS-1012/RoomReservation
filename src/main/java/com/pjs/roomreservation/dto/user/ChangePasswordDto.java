package com.pjs.roomreservation.dto.user;

public class ChangePasswordDto {
    private String email;
    private String currentPw;
    private String newPw;

    public ChangePasswordDto(){}

    public String getEmail() {
        return email;
    }

    public String getCurrentPw() {
        return currentPw;
    }

    public String getNewPw() {
        return newPw;
    }
}
