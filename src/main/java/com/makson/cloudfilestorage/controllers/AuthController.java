package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.UserResponseDto;
import com.makson.cloudfilestorage.dto.ErrorDto;
import com.makson.cloudfilestorage.dto.UserRequestDto;
import com.makson.cloudfilestorage.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Validated UserRequestDto user,
                                    BindingResult bindingResult,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorDto(bindingResult));
        }

        UserResponseDto authResponse = authService.signUp(user, request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Validated UserRequestDto user,
                                    BindingResult bindingResult,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorDto(bindingResult));
        }

        UserResponseDto authResponse = authService.signIn(user, request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) {
        authService.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    private ErrorDto createErrorDto(BindingResult bindingResult) {
        return new ErrorDto(bindingResult.getFieldErrors().stream()
                .map(er -> er.getField() + ": " + er.getDefaultMessage())
                .toList()
                .getFirst()
        );
    }
}
