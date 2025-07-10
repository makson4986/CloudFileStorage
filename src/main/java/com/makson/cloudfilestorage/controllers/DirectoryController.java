package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ResourceRequestDto;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.services.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/directory")
public class DirectoryController {
    private final DirectoryService directoryService;

    @PostMapping
    public ResponseEntity<?> createEmptyDirectory(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        ResourceResponseDto result = directoryService.createEmpty(resourceRequestDto.path(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
