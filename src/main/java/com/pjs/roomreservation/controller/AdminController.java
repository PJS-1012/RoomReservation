package com.pjs.roomreservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Controller", description = "관리자 권한 확인")
public class AdminController {

    @GetMapping("/ping")
    @Operation(summary = "관리자 권한 있으면 OK")
    public String ping(){
        return "OK";
    }
}
