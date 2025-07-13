package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.ResourceRequestDto;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.services.ResourceService;
import com.makson.cloudfilestorage.utils.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<?> getInfo(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathRelativeUserDirectory(resourceRequestDto.path(), user);
        ResourceResponseDto resourceInfo = resourceService.getInfo(path);
        return ResponseEntity.ok(resourceInfo);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathRelativeUserDirectory(resourceRequestDto.path(), user);
        resourceService.delete(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathRelativeUserDirectory(resourceRequestDto.path(), user);
        InputStream downloadedResource = resourceService.download(path);
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
            @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathRelativeUserDirectory(resourceRequestDto.path(), user);
        List<ResourceResponseDto> result = resourceService.upload(path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
