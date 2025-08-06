package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.MovingResourceDto;
import com.makson.cloudfilestorage.dto.QueryDto;
import com.makson.cloudfilestorage.dto.ResourceRequestDto;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.services.ResourceService;
import com.makson.cloudfilestorage.utils.PathUtil;
import com.makson.cloudfilestorage.validation.groups.PathNotBlankCheck;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<?> getInfo(@Validated ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathWithIdentificationDirectory(resourceRequestDto.path(), user);
        return ResponseEntity.ok(resourceService.getInfo(path));
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@Validated({Default.class, PathNotBlankCheck.class}) ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathWithIdentificationDirectory(resourceRequestDto.path(), user);
        String fileName = PathUtil.getName(resourceRequestDto.path()).replace("/", "");
        InputStreamResource resource = new InputStreamResource(resourceService.download(path));

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@Validated QueryDto queryDto, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resourceService.search(user, queryDto.query()));
    }

    @GetMapping("/move")
    public ResponseEntity<?> renameOrMove(@Validated MovingResourceDto movingResourceDto, @AuthenticationPrincipal User user) {
        String fromPath = PathUtil.getFullPathWithIdentificationDirectory(movingResourceDto.from(), user);
        String toPath = PathUtil.getFullPathWithIdentificationDirectory(movingResourceDto.to(), user);

        return ResponseEntity.ok(resourceService.renameOrMove(fromPath, toPath));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@Validated({Default.class, PathNotBlankCheck.class}) ResourceRequestDto resourceRequestDto, @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathWithIdentificationDirectory(resourceRequestDto.path(), user);
        resourceService.delete(path);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @RequestPart("object") List<MultipartFile> files,
            @Validated ResourceRequestDto resourceRequestDto,
            @AuthenticationPrincipal User user) {
        String path = PathUtil.getFullPathWithIdentificationDirectory(resourceRequestDto.path(), user);
        List<ResourceResponseDto> result = resourceService.upload(path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
