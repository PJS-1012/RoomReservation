package com.pjs.roomreservation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 DTO")
public class LoginDto {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력하세요.")
    @Schema(description = "이메일", example = "a@example.com")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Schema(description = "비밀번호", example = "q1w2e3r4")
    private String password;

    public LoginDto () {}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
