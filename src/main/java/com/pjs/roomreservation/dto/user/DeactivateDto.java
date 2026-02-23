package com.pjs.roomreservation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원탈퇴 DTO")
public class DeactivateDto {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력하세요")
    @Schema(description = "이메일", example = "a@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "q1w2e3r4")
    @NotBlank(message = "비밀번호를 입력하세요.")
    private String currentPw;

    public DeactivateDto() {}

    public String getEmail() {
        return email;
    }

    public String getCurrentPw() {
        return currentPw;
    }
}
