package com.pjs.roomreservation.dto.user;

public class DeactivateDto {
    private String email;
    private String currentPw;

    public DeactivateDto() {}

    public String getEmail() {
        return email;
    }

    public String getCurrentPw() {
        return currentPw;
    }
}
