package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(new UserResponseDto(user.getUsername()));
    }
}
