package com.pjs.roomreservation.config;

import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class BootstrapAdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties props;

    @Bean
    public ApplicationRunner initAdmin() {
        return args -> {

            if (userRepository.existsByEmail(props.email())) return;

            User admin = new User(
                    props.email(),
                    passwordEncoder.encode(props.password()),
                    props.name()
            );

            admin.setAdmin();
            userRepository.save(admin);
        };
    }
}
