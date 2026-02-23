package com.pjs.roomreservation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 DTO")
public class ChangePasswordDto {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력하세요")
    @Schema(description = "이메일", example = "a@example.com")
    private String email;

    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    @Schema(description = "현재 비밀번호", example = "currentPassword1")
    private String currentPw;

    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Size(min = 4, max = 50, message = "비밀번호 길이는 7~50자입니다.")
    @Schema(description = "새 비밀번호", example = "newPassword1")
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
