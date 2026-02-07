package com.pjs.roomreservation.dto.room;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public class RoomResponseDto {
    private final Long id;

    private final String name;


    private final String location;


    private final int capacity;


    private final boolean active;


    private final LocalDateTime createdAt;

    public RoomResponseDto(Long id, String name, String location, int capacity, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
