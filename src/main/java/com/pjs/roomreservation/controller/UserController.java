package com.pjs.roomreservation.controller;


import com.pjs.roomreservation.dto.user.ChangePasswordDto;
import com.pjs.roomreservation.dto.user.DeactivateDto;
import com.pjs.roomreservation.dto.user.UserRegisterDto;
import com.pjs.roomreservation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long register(@Valid @RequestBody UserRegisterDto req){
        return userService.register(req.getEmail(), req.getPassword(), req.getName());
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePw(@Valid @RequestBody ChangePasswordDto req){
        userService.changePw(req.getEmail(), req.getCurrentPw(), req.getNewPw());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@Valid @RequestBody DeactivateDto req){
        userService.deactivateUser(req.getEmail(), req.getCurrentPw());
    }


}
