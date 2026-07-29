package com.pjs.roomreservation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomAvailabilityControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void available_invalidDateFormat_returnsValidationError() throws Exception {
        mockMvc.perform(get("/rooms/available")
                        .param("startAt", "invalid-date")
                        .param("endAt", "2030-02-20T11:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("Validation_Error"))
                .andExpect(jsonPath("$.error.startAt").exists());
    }
}
