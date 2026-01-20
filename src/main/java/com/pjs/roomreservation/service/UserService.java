package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Long register(String email, String password, String name) {
        userRepository.findByEmail(email).ifPresent(u->{
            throw new IllegalStateException("이미 존재하는 이메일입니다");
        });

        User user = new User(email, password, name);
        userRepository.save(user);

        return user.getId();
    }
}
