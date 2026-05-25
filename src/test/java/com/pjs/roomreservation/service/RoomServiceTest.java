package com.pjs.roomreservation.service;

import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.service.exception.DuplicateRoomNameException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void create_duplicateActiveRoomName_throws() {
        roomService.create("Room A", "3F", 4);

        assertThatThrownBy(() -> roomService.create("Room A", "4F", 8))
                .isInstanceOf(DuplicateRoomNameException.class);
    }

    @Test
    void create_afterDeactivate_allowsSameName() {
        Long roomId = roomService.create("Room B", "3F", 4);
        roomService.deactivate(roomId);

        Long recreatedRoomId = roomService.create("Room B", "4F", 8);

        assertThat(recreatedRoomId).isNotEqualTo(roomId);
        assertThat(roomRepository.findAll())
                .filteredOn(room -> room.getName().equals("Room B"))
                .hasSize(2);
        assertThat(roomRepository.findAllByActiveTrueOrderByIdAsc())
                .filteredOn(room -> room.getName().equals("Room B"))
                .extracting("id")
                .containsExactly(recreatedRoomId);
    }
}
