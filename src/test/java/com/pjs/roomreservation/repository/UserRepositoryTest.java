package com.pjs.roomreservation.repository;

import com.pjs.roomreservation.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_user() {
        User user = new User("a@test.com", "1234", "park");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByEmail_user() {
        userRepository.save(new User("b@test.com", "1234", "kim"));

        User found = userRepository.findByEmail("b@test.com").orElseThrow();

        assertThat(found.getEmail()).isEqualTo("b@test.com");
        assertThat(found.getName()).isEqualTo("kim");
    }

    @Test
    void findByEmail_notExist(){
        boolean exists = userRepository.findByEmail("c@test.com").isPresent();

        assertThat(exists).isFalse();
    }
}

