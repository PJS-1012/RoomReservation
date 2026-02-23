package com.pjs.roomreservation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 DTO")
public class UserRegisterDto {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력하세요.")
    @Schema(description = "이메일", example = "a@example.com")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 4, max = 50, message = "비밀번호 길이는 7~50자입니다.")
    @Schema(description = "비밀번호", example = "q1w2e3r4")
    private String password;

    @NotBlank(message = "이름을 입력하세요.")
    @Schema(description = "이름", example = "홍길동")
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
