package com.pjs.roomreservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReservationControllerMockTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RoomRepository roomRepository;

    private void register(String email, String password, String name) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password,
                "name", name
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password
        ));

        String token = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(token).get("accessToken").asText();
    }

    private Long createRoom(String name, String location, int capacity) {
        Room room = new Room(name, location, capacity);
        return roomRepository.save(room).getId();
    }

    @Test
    void reservation_create_201() throws Exception {
        String email = "u1@test.com";
        String pw = "1234";
        register(email, pw, "park");
        String accessToken = loginAndGetAccessToken(email, pw);

        Long roomId = createRoom("회의실A", "3층", 8);

        LocalDateTime startAt = LocalDateTime.of(2026, 2, 20, 10, 0);
        LocalDateTime endAt   = LocalDateTime.of(2026, 2, 20, 11, 0);

        String body = objectMapper.writeValueAsString(Map.of(
                "roomId", roomId,
                "startAt", startAt.toString(),
                "endAt", endAt.toString()
        ));

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void reservation_overlap_409() throws Exception {
        String email = "u2@test.com";
        String pw = "1234";
        register(email, pw, "kim");
        String accessToken = loginAndGetAccessToken(email, pw);

        Long roomId = createRoom("회의실B", "2층", 6);

        LocalDateTime s1 = LocalDateTime.of(2026, 2, 20, 10, 0);
        LocalDateTime e1 = LocalDateTime.of(2026, 2, 20, 11, 0);

        LocalDateTime s2 = LocalDateTime.of(2026, 2, 20, 10, 30);
        LocalDateTime e2 = LocalDateTime.of(2026, 2, 20, 11, 30);

        String body1 = objectMapper.writeValueAsString(Map.of(
                "roomId", roomId,
                "startAt", s1.toString(),
                "endAt", e1.toString()
        ));
        String body2 = objectMapper.writeValueAsString(Map.of(
                "roomId", roomId,
                "startAt", s2.toString(),
                "endAt", e2.toString()
        ));

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void reservation_list_me_200() throws Exception {
        String email = "u3@test.com";
        String pw = "1234";
        register(email, pw, "lee");
        String accessToken = loginAndGetAccessToken(email, pw);

        Long roomId = createRoom("회의실C", "1층", 4);

        LocalDateTime startAt = LocalDateTime.of(2026, 2, 20, 12, 0);
        LocalDateTime endAt   = LocalDateTime.of(2026, 2, 20, 13, 0);

        String body = objectMapper.writeValueAsString(Map.of(
                "roomId", roomId,
                "startAt", startAt.toString(),
                "endAt", endAt.toString()
        ));

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void reservation_cancel_other_user_403() throws Exception {
        String email1 = "a1@test.com";
        String pw1 = "1234";
        register(email1, pw1, "user1");
        String accessToken1 = loginAndGetAccessToken(email1, pw1);

        String email2 = "a2@test.com";
        String pw2 = "1234";
        register(email2, pw2, "user2");
        String accessToken2 = loginAndGetAccessToken(email2, pw2);

        Long roomId = createRoom("회의실D", "4층", 10);

        LocalDateTime startAt = LocalDateTime.of(2026, 2, 21, 10, 0);
        LocalDateTime endAt   = LocalDateTime.of(2026, 2, 21, 11, 0);

        String body = objectMapper.writeValueAsString(Map.of(
                "roomId", roomId,
                "startAt", startAt.toString(),
                "endAt", endAt.toString()
        ));

        String createdJson = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + accessToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long reservationId = createdJson.trim().startsWith("{")
                ? objectMapper.readTree(createdJson).get("id").asLong()
                : Long.valueOf(createdJson.trim());

        mockMvc.perform(delete("/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + accessToken2))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
