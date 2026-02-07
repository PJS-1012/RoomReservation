package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.room.RoomResponseDto;
import com.pjs.roomreservation.service.RoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponseDto> list() {
        return roomService.listRoom().stream()
                .map(r -> new RoomResponseDto(r.getId(), r.getName(), r.getLocation(), r.getCapacity(), r.isActive(), r.getCreatedAt())).toList();
    }

    @GetMapping("/{roomId}")
    public RoomResponseDto get(@PathVariable Long roomId) {
        var r = roomService.getActiveById(roomId);
        return new RoomResponseDto(r.getId(), r.getName(), r.getLocation(), r.getCapacity(), r.isActive(), r.getCreatedAt());
    }
}
