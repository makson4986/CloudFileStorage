package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ResourceRequestDto;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.services.DirectoryService;
import com.makson.cloudfilestorage.services.ResourceService;
import com.makson.cloudfilestorage.utils.PathUtil;
import com.makson.cloudfilestorage.validation.groups.PathNotBlankCheck;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/directory")
public class DirectoryController {
    private final DirectoryService directoryService;
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<?> getContentsInfo(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathWithIdentificationDirectory(resourceRequestDto.path(), user);
        List<ResourceResponseDto> result = resourceService.getContentsInfo(path, false);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createEmptyDirectory(@Validated({Default.class, PathNotBlankCheck.class}) ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathWithIdentificationDirectory(resourceRequestDto.path(), user);
        ResourceResponseDto result = directoryService.createEmpty(path);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
