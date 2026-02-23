package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.room.RoomResponseDto;
import com.pjs.roomreservation.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@Tag(name = "Room Controller", description = "회의실 관련 정보")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    @Operation(summary = "모든 회의실 정보 불러오기")
    public List<RoomResponseDto> list() {
        return roomService.listRoom().stream()
                .map(r -> new RoomResponseDto(r.getId(), r.getName(), r.getLocation(), r.getCapacity(), r.isActive(), r.getCreatedAt())).toList();
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "해당 회의실 정보 불러오기")
    public RoomResponseDto get(@PathVariable Long roomId) {
        var r = roomService.getActiveById(roomId);
        return new RoomResponseDto(r.getId(), r.getName(), r.getLocation(), r.getCapacity(), r.isActive(), r.getCreatedAt());
    }
}
