package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ErrorDto;
import com.makson.cloudfilestorage.exceptions.UserAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.UserNotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<?> handleRegistrationException(Exception ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class, UserNotAuthorizedException.class})
    public ResponseEntity<?> handleAuthenticationException(Exception ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception ex) {
        log.error(ex.getMessage());
        return ResponseEntity.internalServerError().body(new ErrorDto(ex.getMessage()));
    }
}
