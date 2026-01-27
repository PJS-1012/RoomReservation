package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.auth.LoginDto;
import com.pjs.roomreservation.dto.auth.TokenResponseDto;
import com.pjs.roomreservation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponseDto login(@Valid @RequestBody LoginDto req){
        String token = authService.login(req.getEmail(), req.getPassword());
        return new TokenResponseDto(token);
    }
}
