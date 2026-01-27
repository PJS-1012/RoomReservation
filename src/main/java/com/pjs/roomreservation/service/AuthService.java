package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.UserRepository;
import com.pjs.roomreservation.security.JwtTokenProvider;
import com.pjs.roomreservation.service.exception.InvalidCredentialException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder pwEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder pwEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.pwEncoder = pwEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialException::new);

        if(!pwEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialException();
        }

        return jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
    }
}
