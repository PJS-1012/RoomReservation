package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.service.exception.DuplicateRoomNameException;
import com.pjs.roomreservation.service.exception.RoomNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Long create(String name, String location, int capacity) {
        if(roomRepository.existByName(name)){
            throw new DuplicateRoomNameException(name);
        }

        Room room = new Room(name, location, capacity);

        roomRepository.save(room);

        return room.getId();
    }

    public Room getActiveById(Long id) {
        return roomRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new RoomNotFoundException(id));
    }

    public List<Room> listRoom() {
        return roomRepository.findAllByActiveTrueOrderByIdAsc();
    }

    @Transactional
    public void update(Long id, String name, String location, int capacity){
        Room room = getActiveById(id);

        if(!room.getName().equals(name) && roomRepository.existByName(name)){
            throw new DuplicateRoomNameException(name);
        }

        room.update(name, location, capacity);
    }

    @Transactional
    public void deactivate(Long id){
        Room room = getActiveById(id);

        room.deactivate();
    }
}
