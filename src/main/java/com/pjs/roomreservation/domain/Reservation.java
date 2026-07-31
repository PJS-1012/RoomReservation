package com.pjs.roomreservation.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservation",
        indexes = {
                @Index(
                        name = "idx_reservation_room_active_time",
                        columnList = "room_id, canceled, start_at, end_at"
                ),
                @Index(
                        name = "idx_reservation_user_start_at",
                        columnList = "user_id, start_at"
                ),
                @Index(
                        name = "idx_reservation_room_created_at_id",
                        columnList = "room_id, created_at, id"
                )
        }
)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable =false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean canceled;

    protected Reservation() {}

    public Reservation(User user, Room room, LocalDateTime startAt, LocalDateTime endAt) {
        this.user = user;
        this.room = room;
        this.startAt = startAt;
        this.endAt = endAt;
        this.canceled = false;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void cancel() {
        this.canceled = true;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isCanceled() {
        return canceled;
    }
}
