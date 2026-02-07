package com.pjs.roomreservation.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Room() {}

    public Room(String name, String location, int capacity) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.active = true;
    }

    public void update(String name, String location, int capacity){
        this.name = name;
        this.location = location;
        this.capacity = capacity;
    }

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    public void deactivate(){
        active = false;
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
