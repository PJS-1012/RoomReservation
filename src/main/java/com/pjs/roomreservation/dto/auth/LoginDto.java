package com.pjs.roomreservation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginDto {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력하세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    public LoginDto () {}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
