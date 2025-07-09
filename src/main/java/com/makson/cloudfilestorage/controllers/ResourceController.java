package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ResourceRequestDto;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.services.ResourceService;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.errors.MinioException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<?> getInfo(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        ResourceResponseDto resourceInfo = resourceService.getInfo(resourceRequestDto.path(), user.getId());
        return ResponseEntity.ok(resourceInfo);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        resourceService.delete(resourceRequestDto.path(), user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        InputStream downloadedResource = resourceService.download(resourceRequestDto.path(), user.getId());
        InputStreamResource resource = new InputStreamResource(downloadedResource);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(PathUtil.getName(resourceRequestDto.path()), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @RequestPart("object") List<MultipartFile> files,
            @Validated ResourceRequestDto resourceRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        List<ResourceResponseDto> result = resourceService.upload(resourceRequestDto.path(), files, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
