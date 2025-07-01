package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.repositories.MinioRepository;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final MinioRepository minioRepository;
    private final long DIRECTORY_SIZE = 0;

    public ResourceResponseDto getInfo(String path, int userId) {
        if (isDirectoryExists(path, userId)) {
            return new ResourceResponseDto(
                    path,
                    getDirectoryName(path),
                    DIRECTORY_SIZE,
                    Resource.DIRECTORY
            );
        }

        throw new ResourceNotFoundException("Resource not found");
    }

    public void delete(String path, int userId) {
        if (!isDirectoryExists(path, userId)) {
            throw new ResourceNotFoundException("Resource not found");
        }

        minioRepository.deleteDirectory(path, userId);
    }

    public boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    private boolean isDirectoryExists(String path, int userId) {
        Iterable<Result<Item>> resources = minioRepository.getFilesInDirectory(path, userId, false);
        return resources.iterator().hasNext();
    }

    private String getDirectoryName(String path) {
        var part = path.split("/");
        return part[part.length - 1];
    }
}
