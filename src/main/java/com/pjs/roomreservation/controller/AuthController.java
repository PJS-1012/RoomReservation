package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.auth.LoginDto;
import com.pjs.roomreservation.dto.auth.TokenResponseDto;
import com.pjs.roomreservation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
