package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.repositories.MinioRepository;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioRepository minioRepository;

    public ResourceResponseDto getInfo(String path, int userId) {
        Optional<StatObjectResponse> fileInfo = minioRepository.getFileInfo(path, userId);

        if (fileInfo.isPresent()) {
            return new ResourceResponseDto(
                    path,
                    getFileName(path),
                    fileInfo.get().size(),
                    Resource.FILE
            );
        }

        throw new ResourceNotFoundException("Resource not found");
    }

    public void delete(String path, int userId) {
        if (!isFileExists(path, userId)) {
            throw new ResourceNotFoundException("Resource not found");

        }

        minioRepository.deleteFile(path, userId);
    }

    public ResourceResponseDto upload(String pathTo, MultipartFile file, int userId) {
        String path = pathTo + "/" + file.getOriginalFilename();

        if (isFileExists(path, userId)) {
            throw new ResourceAlreadyExistException("File '%s' already exists".formatted(file.getOriginalFilename()));
        }

        minioRepository.uploadFile(path, file, userId);
        return getInfo(path, userId);
    }

    public boolean isFileExists(String path, int userId) {
        Optional<StatObjectResponse> fileInfo = minioRepository.getFileInfo(path, userId);
        return fileInfo.isPresent();
    }

    private String getFileName(String path) {
        var part = path.split("/");
        return part[part.length - 1];
    }
}
