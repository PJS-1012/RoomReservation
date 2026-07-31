package com.pjs.roomreservation.global.exception;

import com.pjs.roomreservation.service.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {
    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("Duplicate_Email", e.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPassword(InvalidPasswordException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of("Invalid_Password", e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("User_Not_Found", e.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ApiError> handleInvalidCredential(InvalidCredentialException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of("Invalid_Credential", e.getMessage()));
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiError> handleRoomNotFound(RoomNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("Room_Not_Found", e.getMessage()));
    }

    @ExceptionHandler(DuplicateRoomNameException.class)
    public ResponseEntity<ApiError> handleDuplicateRoomName(DuplicateRoomNameException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("Duplicate_Room_Name", e.getMessage()));
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ApiError> handleReservationConflict(ReservationConflictException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("Reservation_Conflict", e.getMessage()));
    }

    @ExceptionHandler(ReservationLockException.class)
    public ResponseEntity<ApiError> handleReservationLock(ReservationLockException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("Reservation_Lock_Conflict", e.getMessage()));
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiError> handleReservationNotFound(ReservationNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("Reservation_Not_Found", e.getMessage()));
    }

    @ExceptionHandler(ReservationForbiddenException.class)
    public ResponseEntity<ApiError> handleReservationForbidden(ReservationForbiddenException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of("Reservation_Forbidden", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of("Bad_Request", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e){
        return validationError(e.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException e) {
        return validationError(e.getBindingResult());
    }

    private ResponseEntity<ApiError> validationError(BindingResult bindingResult) {
        Map<String, String> error = new LinkedHashMap<>();
        for(FieldError fe : bindingResult.getFieldErrors()){
            error.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of("Validation_Error", "요청 값이 올바르지 않습니다.", error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEtc(Exception e){
        log.error("unexpected_api_error", e);
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
