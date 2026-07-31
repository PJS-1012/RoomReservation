package com.pjs.roomreservation.config;

import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BootstrapAdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties props;

    @Bean
    public ApplicationRunner initAdmin() {
        return args -> {
            try {
                if (props.email().isBlank() || props.password().isBlank() || props.name().isBlank()) {
                    log.warn("Bootstrap admin initialization skipped because required properties are missing.");
                    return;
                }

                if (userRepository.existsByEmail(props.email())) return;

                User admin = new User(
                        props.email(),
                        passwordEncoder.encode(props.password()),
                        props.name()
                );

                admin.setAdmin();
                userRepository.saveAndFlush(admin);
            } catch (DataIntegrityViolationException e) {
                log.info("Bootstrap admin was created by another instance. email={}", props.email());
            }
        };
    }
}
