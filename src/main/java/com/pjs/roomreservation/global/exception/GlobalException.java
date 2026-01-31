package com.pjs.roomreservation.global.exception;

import com.pjs.roomreservation.service.exception.DuplicateEmailException;
import com.pjs.roomreservation.service.exception.InvalidCredentialException;
import com.pjs.roomreservation.service.exception.InvalidPasswordException;
import com.pjs.roomreservation.service.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ApiError> handleInvalidCredential(InvalidCredentialException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of("Invalid_Credential", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e){
        Map<String, String> error = new LinkedHashMap<>();
        for(FieldError fe : e.getBindingResult().getFieldErrors()){
            error.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of("Validation_Error", "요청 값이 올바르지 않습니다.", error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEtc(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of("Internal_Error", "서버 오류"));
    }

    public record ApiError(String code, String message, Instant timeStamp, Map<String, String> error){
        public static ApiError of(String code, String message, Map<String, String> error){
            return new ApiError(code, message, Instant.now(), error);
        }
        public static ApiError of(String code, String message){
            return new ApiError(code, message, Instant.now(), null);
        }
    }


}
