package com.pjs.roomreservation.dto.user;

public class UserRegisterDto {
    private String email;
    private String password;
    private String name;

    public UserRegisterDto(){}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
