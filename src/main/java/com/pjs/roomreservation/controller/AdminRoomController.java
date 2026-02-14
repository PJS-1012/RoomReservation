package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.room.RoomCreateDto;
import com.pjs.roomreservation.dto.room.RoomUpdateDto;
import com.pjs.roomreservation.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminRoomController {
    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@Valid @RequestBody RoomCreateDto req) {
        return roomService.create(req.getName(), req.getLocation(), req.getCapacity());
    }

    @PutMapping("{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long roomId, @Valid @RequestBody RoomUpdateDto req){
        roomService.update(roomId, req.getName(), req.getLocation(), req.getCapacity());
    }

    @DeleteMapping("{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long roomId){
        roomService.deactivate(roomId);
    }
}
