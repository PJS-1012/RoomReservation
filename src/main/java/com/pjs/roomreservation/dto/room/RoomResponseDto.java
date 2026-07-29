package com.pjs.roomreservation.dto.room;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
@Schema(description = "회의실 정보 DTO")
public class RoomResponseDto {
    private final Long id;

    private final String name;


    private final String location;


    private final int capacity;


    private final boolean active;


    private final LocalDateTime createdAt;

    @JsonCreator
    public RoomResponseDto(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("location") String location,
            @JsonProperty("capacity") int capacity,
            @JsonProperty("active") boolean active,
            @JsonProperty("createdAt") LocalDateTime createdAt
    ) {
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
