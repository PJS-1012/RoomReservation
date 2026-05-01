package com.pjs.roomreservation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "본인 조회 DTO")
public class UserMeResponseDto {

    private final Long id;
    private final String email;
    private final String name;
    private final boolean active;
    private final boolean admin;
    private final LocalDateTime createdAt;

    public UserMeResponseDto(Long id, String email, String name, boolean active, boolean admin, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.active = active;
        this.admin = admin;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAdmin() {
        return admin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
