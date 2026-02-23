package com.pjs.roomreservation.controller;

import com.pjs.roomreservation.dto.auth.LoginDto;
import com.pjs.roomreservation.dto.auth.TokenResponseDto;
import com.pjs.roomreservation.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "로그인으로 사용자 인증")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public TokenResponseDto login(@Valid @RequestBody LoginDto req){
        String token = authService.login(req.getEmail(), req.getPassword());
        return new TokenResponseDto(token);
    }
}
