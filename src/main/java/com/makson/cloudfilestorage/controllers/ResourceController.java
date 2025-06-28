package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ResourceRequestDto;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.services.ResourceService;
import io.minio.errors.MinioException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping()
    public ResponseEntity<?> getInfo(
            @Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails user) throws GeneralSecurityException, MinioException, IOException {
        ResourceResponseDto resourceInfo = resourceService.getInfo(resourceRequestDto.path(), user);
        return ResponseEntity.ok(resourceInfo);
    }

    @DeleteMapping()
    public ResponseEntity<?> delete(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails user) throws GeneralSecurityException, MinioException, IOException {
        resourceService.delete(resourceRequestDto.path(), user);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<?> download(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails user) throws GeneralSecurityException, MinioException, IOException {
        return ResponseEntity.ok("ok");
    }

    @PostMapping()
    public ResponseEntity<?> upload(
            @RequestPart("object") List<MultipartFile> files,
            @Validated ResourceRequestDto resourceRequestDto,
            @AuthenticationPrincipal UserDetails user) throws GeneralSecurityException, MinioException, IOException {
        resourceService.upload(resourceRequestDto.path(), user, files);
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }
}
