CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_admin BIT(1) NOT NULL,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    capacity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    location VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    canceled BIT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    start_at DATETIME(6) NOT NULL,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_reservation_room_active_time (room_id, canceled, start_at, end_at),
    KEY idx_reservation_user_start_at (user_id, start_at),
    KEY idx_reservation_room_created_at_id (room_id, created_at, id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
