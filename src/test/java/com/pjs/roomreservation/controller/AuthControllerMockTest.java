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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_200() throws Exception {
        String register = objectMapper.writeValueAsString(Map.of(
                "email", "a@test.com",
                "password", "1234",
                "name", "park"
        ));

        String login = objectMapper.writeValueAsString(Map.of(
                "email", "a@test.com",
                "password", "1234"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_401() throws Exception {
        String register = objectMapper.writeValueAsString(Map.of(
                "email", "b@test.com",
                "password", "1234",
                "name", "park"
        ));

        String login1 = objectMapper.writeValueAsString(Map.of(
                "email", "bb@test.com",
                "password", "1234"
        ));


        String login2 = objectMapper.writeValueAsString(Map.of(
                "email", "b@test.com",
                "password", "11234"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login1))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login2))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }


}
