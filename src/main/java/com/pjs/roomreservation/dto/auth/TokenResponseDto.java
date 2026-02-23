package com.pjs.roomreservation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 DTO")
public class TokenResponseDto {
    private final String accessToken;

    public TokenResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
