package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.UserRepository;
import com.pjs.roomreservation.service.exception.DuplicateEmailException;
import com.pjs.roomreservation.service.exception.InvalidPasswordException;
import com.pjs.roomreservation.service.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder pwEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder pwEncoder){
        this.userRepository = userRepository;
        this.pwEncoder = pwEncoder;
    }

    @Transactional
    public Long register(String email, String password, String name) {
        if(userRepository.existsByEmail(email)){
            throw new DuplicateEmailException(email);
        }

        String enPw = pwEncoder.encode(password);

        User user = new User(email, enPw, name);
        userRepository.save(user);

        return user.getId();
    }

    public User getById(Long userId){
        return userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException(email));
    }

    @Transactional
    public void changePw(String email, String currentPw, String newPw){
        User user = getByEmail(email);

        if(!pwEncoder.matches(currentPw, user.getPassword())){
            throw new InvalidPasswordException();
        }

        String enPw = pwEncoder.encode(newPw);
        user.setPassword(enPw);
    }

    @Transactional
    public void deactivateUser(String email, String currentPw) {
        User user = getByEmail(email);
        if(!pwEncoder.matches(currentPw, user.getPassword())){
            throw new InvalidPasswordException();
        }
        user.deactivate();
    }



}
