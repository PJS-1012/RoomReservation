package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.global.cache.RoomChangedEvent;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.service.exception.DuplicateRoomNameException;
import com.pjs.roomreservation.service.exception.RoomNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RoomService(RoomRepository roomRepository, ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long create(String name, String location, int capacity) {
        if(roomRepository.existsByNameAndActiveTrue(name)){
            throw new DuplicateRoomNameException(name);
        }

        Room room = new Room(name, location, capacity);

        roomRepository.save(room);
        eventPublisher.publishEvent(new RoomChangedEvent());

        return room.getId();
    }

    public Room getActiveById(Long id) {
        return roomRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new RoomNotFoundException(id));
    }

    @Transactional
    public void update(Long id, String name, String location, int capacity){
        Room room = getActiveById(id);

        if(!room.getName().equals(name) && roomRepository.existsByNameAndActiveTrue(name)){
            throw new DuplicateRoomNameException(name);
        }

        room.update(name, location, capacity);
        eventPublisher.publishEvent(new RoomChangedEvent());
    }

    @Transactional
    public void deactivate(Long id){
        Room room = getActiveById(id);

        room.deactivate();
        eventPublisher.publishEvent(new RoomChangedEvent());
    }
}
