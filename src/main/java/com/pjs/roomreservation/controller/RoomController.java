package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.room.AvailableRoomSearchDto;
import com.pjs.roomreservation.dto.room.RoomResponseDto;
import com.pjs.roomreservation.service.RoomAvailabilityService;
import com.pjs.roomreservation.service.RoomQueryService;
import com.pjs.roomreservation.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final RoomQueryService roomQueryService;
    private final RoomAvailabilityService roomAvailabilityService;

    public RoomController(
            RoomService roomService,
            RoomQueryService roomQueryService,
            RoomAvailabilityService roomAvailabilityService
    ) {
        this.roomService = roomService;
        this.roomQueryService = roomQueryService;
        this.roomAvailabilityService = roomAvailabilityService;
    }

    @GetMapping
    @Operation(summary = "모든 회의실 정보 불러오기")
    public List<RoomResponseDto> list() {
        return roomQueryService.listActiveRooms();
    }

    @GetMapping("/available")
    @Operation(summary = "예약 가능한 회의실 조회")
    public List<RoomResponseDto> available(@Valid @ModelAttribute AvailableRoomSearchDto req) {
        return roomAvailabilityService.findAvailableRooms(
                req.getStartAt(),
                req.getEndAt(),
                req.getCapacity()
        );
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "해당 회의실 정보 불러오기")
    public RoomResponseDto get(@PathVariable Long roomId) {
        var r = roomService.getActiveById(roomId);
        return new RoomResponseDto(r.getId(), r.getName(), r.getLocation(), r.getCapacity(), r.isActive(), r.getCreatedAt());
    }
}
