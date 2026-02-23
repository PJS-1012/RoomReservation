package com.pjs.roomreservation.controller;


import com.pjs.roomreservation.dto.user.ChangePasswordDto;
import com.pjs.roomreservation.dto.user.DeactivateDto;
import com.pjs.roomreservation.dto.user.UserMeResponseDto;
import com.pjs.roomreservation.dto.user.UserRegisterDto;
import com.pjs.roomreservation.security.UserPrincipal;
import com.pjs.roomreservation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User Controller", description = "각종 사용자 관련 기능")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입")
    public Long register(@Valid @RequestBody UserRegisterDto req){
        return userService.register(req.getEmail(), req.getPassword(), req.getName());
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "비밀번호 변경")
    public void changePw(@AuthenticationPrincipal UserPrincipal userprincipal, @Valid @RequestBody ChangePasswordDto req){
        userService.changePw(userprincipal.getEmail(), req.getCurrentPw(), req.getNewPw());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "사용자 회원 탈퇴(비활성화)")
    public void deactivate(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody DeactivateDto req){
        userService.deactivateUser(userPrincipal.getEmail(), req.getCurrentPw());
    }

    @GetMapping("/me")
    @Operation(summary = "사용자 본인 정보 불러오기")
    public UserMeResponseDto me(@AuthenticationPrincipal UserPrincipal userPrincipal){
        var user = userService.getActiveById(userPrincipal.getUserId());
        return new UserMeResponseDto(
                user.getId(),
                user.getPassword(),
                user.getEmail(),
                user.getName(),
                user.isActive(),
                user.isAdmin(),
                user.getCreatedAt()
        );
    }


}
