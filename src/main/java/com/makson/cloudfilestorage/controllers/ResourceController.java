package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ResourceDto;
import com.makson.cloudfilestorage.services.ResourceService;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping()
    public ResponseEntity<?> getInfo(@RequestParam String path, @AuthenticationPrincipal UserDetails user) throws GeneralSecurityException, MinioException, IOException {
        ResourceDto resourceInfo = resourceService.getInfo(path, user);
        return ResponseEntity.ok(resourceInfo);
    }

}
