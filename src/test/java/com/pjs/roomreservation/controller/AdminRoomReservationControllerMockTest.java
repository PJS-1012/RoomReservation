package com.pjs.roomreservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pjs.roomreservation.domain.Reservation;
import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.ReservationRepository;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminRoomReservationControllerMockTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User createUser(String email, boolean admin) {
        User user = new User(email, passwordEncoder.encode("1234"), email.substring(0, email.indexOf("@")));
        if (admin) {
            user.setAdmin();
        }
        return userRepository.save(user);
    }

    private String login(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "1234"
        ));

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private Room createRoom(String name) {
        return roomRepository.save(new Room(name, "test-location", 10));
    }

    private Reservation createReservation(User user, Room room, int day, int hour) {
        LocalDateTime startAt = LocalDateTime.of(2030, 1, day, hour, 0);
        LocalDateTime endAt = startAt.plusHours(1);
        return reservationRepository.saveAndFlush(new Reservation(user, room, startAt, endAt));
    }

    @Test
    void admin_can_query_room_reservations() throws Exception {
        User admin = createUser("admin-query@test.com", true);
        User user = createUser("user-query@test.com", false);
        Room room = createRoom("admin-query-room");
        Reservation reservation = createReservation(user, room, 10, 10);
        String adminToken = login(admin.getEmail());

        mockMvc.perform(get("/admin/rooms/{roomId}/reservations", room.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reservationId").value(reservation.getId()))
                .andExpect(jsonPath("$[0].roomId").value(room.getId()))
                .andExpect(jsonPath("$[0].roomName").value(room.getName()))
                .andExpect(jsonPath("$[0].userId").value(user.getId()))
                .andExpect(jsonPath("$[0].userName").value(user.getName()))
                .andExpect(jsonPath("$[0].userEmail").value(user.getEmail()))
                .andExpect(jsonPath("$[0].startAt").exists())
                .andExpect(jsonPath("$[0].endAt").exists())
                .andExpect(jsonPath("$[0].canceled").value(false))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void user_cannot_query_admin_room_reservations() throws Exception {
        User user = createUser("user-forbidden@test.com", false);
        Room room = createRoom("user-forbidden-room");
        String userToken = login(user.getEmail());

        mockMvc.perform(get("/admin/rooms/{roomId}/reservations", room.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_query_nonexistent_room_returns_404() throws Exception {
        User admin = createUser("admin-missing-room@test.com", true);
        String adminToken = login(admin.getEmail());

        mockMvc.perform(get("/admin/rooms/{roomId}/reservations", 999999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void admin_room_reservations_return_all_rows_even_when_page_parameters_are_sent() throws Exception {
        User admin = createUser("admin-paging@test.com", true);
        User user = createUser("user-paging@test.com", false);
        Room room = createRoom("admin-paging-room");
        createReservation(user, room, 11, 10);
        createReservation(user, room, 12, 10);
        createReservation(user, room, 13, 10);
        String adminToken = login(admin.getEmail());

        mockMvc.perform(get("/admin/rooms/{roomId}/reservations?page=0&size=2", room.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
