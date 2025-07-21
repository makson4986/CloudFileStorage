package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ErrorDto;
import com.makson.cloudfilestorage.exceptions.ResourceAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.exceptions.UserAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler({
            ResourceAlreadyExistException.class,
            UserAlreadyExistException.class
    })
    public ResponseEntity<?> handleConflictException(Exception ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MultipartException.class
    })
    public ResponseEntity<?> handleBadRequestException(Exception ex) {
        ErrorDto errorDto;

        if (ex instanceof MethodArgumentNotValidException) {
            errorDto = new ErrorDto(
                    ((MethodArgumentNotValidException) ex).getFieldErrors().stream()
                            .map(er -> er.getField() + ": " + er.getDefaultMessage())
                            .toList()
                            .getFirst()
            );
        } else {
            errorDto = new ErrorDto("Files are invalid or missing");
        }

        log.warn(errorDto.message());
        return ResponseEntity.badRequest().body(errorDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleAuthenticationException(Exception ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleGetInfoResourceException(Exception ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception ex) {
        log.error(ex.getMessage());
        return ResponseEntity.internalServerError().body(new ErrorDto(ex.getMessage()));
    }
}
