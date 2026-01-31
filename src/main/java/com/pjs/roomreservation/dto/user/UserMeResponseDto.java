package com.pjs.roomreservation.dto.user;

import java.time.LocalDateTime;

public class UserMeResponseDto {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final boolean isAdmin;
    private final boolean active;
    private final LocalDateTime createdAt;

    public UserMeResponseDto(Long id, String email, String password, String name, boolean isAdmin, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.isAdmin = isAdmin;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
