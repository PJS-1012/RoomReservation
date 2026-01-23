package com.pjs.roomreservation.global.exception;

import com.pjs.roomreservation.service.exception.DuplicateEmailException;
import com.pjs.roomreservation.service.exception.InvalidPasswordException;
import com.pjs.roomreservation.service.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("Duplicate_Email", e.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPassword(InvalidPasswordException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of("Invalide_Password", e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("User_Not_Found", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEtc(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of("Internal_Error", "서버 오류"));
    }

    public record ApiError(String code, String message, Instant timeStamp){
        public static ApiError of(String code, String message){
            return new ApiError(code, message, Instant.now());
        }
    }


}
