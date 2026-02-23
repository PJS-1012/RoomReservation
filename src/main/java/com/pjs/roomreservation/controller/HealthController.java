package com.pjs.roomreservation.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Ping", description = "서버 작동 여부 확인")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "서버 정상 작동 시 OK")
    public String health() {
        return "OK";
    }


}
