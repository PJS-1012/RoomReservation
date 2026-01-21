package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder pwEncoder;

    @Test
    void register_en() {
        String email = "a@test.com";
        String password = "1234";
        String name = "park";

        Long userId = userService.register(email, password, name);
        User saved = userRepository.findById(userId).orElseThrow();

        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getPassword()).isNotEqualTo(password);
        assertThat(pwEncoder.matches(password, saved.getPassword())).isTrue();

        System.out.println("saved.getPassword() = " + saved.getPassword());
    }
}
