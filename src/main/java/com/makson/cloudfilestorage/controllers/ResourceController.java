package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ResourceRequestDto;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.services.ResourceService;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping()
    public ResponseEntity<?> getInfo(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails user) throws GeneralSecurityException, MinioException, IOException {
        ResourceResponseDto resourceInfo = resourceService.getInfo(resourceRequestDto.path(), user);
        return ResponseEntity.ok(resourceInfo);
    }

    @DeleteMapping()
    public ResponseEntity<?> delete(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails user) throws GeneralSecurityException, MinioException, IOException {
        resourceService.delete(resourceRequestDto.path(), user);
        return ResponseEntity.ok("ok");
    }
}
