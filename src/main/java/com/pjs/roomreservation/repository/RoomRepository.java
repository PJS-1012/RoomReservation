package com.pjs.roomreservation.repository;

import com.pjs.roomreservation.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByName(String name);
    Optional<Room> findByIdAndActiveTrue(Long Id);
    List<Room> findAllByActiveTrueOrderByIdAsc();
}
