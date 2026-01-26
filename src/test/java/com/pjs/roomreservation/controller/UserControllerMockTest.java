package com.pjs.roomreservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerMockTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void register_201() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "a@test.com",
                "password", "1234",
                "name", "park"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void duplicateEmail_409() throws Exception {
        String body1 = objectMapper.writeValueAsString(Map.of(
                "email", "b@test.com",
                "password", "1111",
                "name", "kim"
        ));

        String body2 = objectMapper.writeValueAsString(Map.of(
                "email", "b@test.com",
                "password", "2222",
                "name", "park"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timeStamp").exists());
    }

    @Test
    void changePw_wrongPassword_401() throws Exception {
        String register = objectMapper.writeValueAsString(Map.of(
                "email", "c@test.com",
                "password", "1111",
                "name", "park"
        ));

        String changePw = objectMapper.writeValueAsString(Map.of(
                "email", "c@test.com",
                "currentPw", "2222",
                "newPw", "1234"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePw))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deactivate_401() throws Exception {

        String register = objectMapper.writeValueAsString(Map.of(
                "email", "d@test.com",
                "password", "1234",
                "name", "park"
        ));

        String wrongPw = objectMapper.writeValueAsString(Map.of(
                "email", "d@test.com",
                "currentPw", "1111"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPw))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deactivate_404() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "e@test.com",
                "currentPw", "1234"
        ));

        mockMvc.perform(delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_400() throws Exception {
        String register1 = objectMapper.writeValueAsString(Map.of(
                "email", "atestcom",
                "password", "1234",
                "name", "park"
        ));

        String register2 = objectMapper.writeValueAsString(Map.of(
                "email", "",
                "password", "1234",
                "name", "kim"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register1))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("Validation_Error"))
                .andExpect(jsonPath("$.error.email").value("이메일 형식이 올바르지 않습니다."));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register2))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("Validation_Error"))
                .andExpect(jsonPath("$.error.email").value("이메일을 입력하세요."));

    }

}
