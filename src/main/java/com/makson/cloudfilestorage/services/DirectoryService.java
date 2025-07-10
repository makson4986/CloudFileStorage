package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.repositories.MinioRepository;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.Result;
import io.minio.messages.Item;
import io.swagger.v3.core.util.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final MinioRepository minioRepository;
    private final long DIRECTORY_SIZE = 0;


    public ResourceResponseDto createEmpty(String path, int userId) {
        String parentDirectory = PathUtil.getParent(path);
        String name = PathUtil.getName(path);

        if (!isDirectoryExists(parentDirectory, userId)) {
            throw new ResourceNotFoundException("Parent directory does not exist");
        }

        if (isDirectoryExists(path, userId)) {
            throw new ResourceAlreadyExistException("Directory '%s' already exists".formatted(name));
        }

        minioRepository.createEmptyDirectory(path, userId);

        return new ResourceResponseDto(parentDirectory, name, DIRECTORY_SIZE, Resource.DIRECTORY);
    }

    public ResourceResponseDto getInfo(String path, int userId) {
        if (isDirectoryExists(path, userId)) {
            return new ResourceResponseDto(
                    PathUtil.getParent(path),
                    PathUtil.getName(path),
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

    public InputStream download(String path, int userId) {
        return null;
    }

    public boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    private boolean isDirectoryExists(String path, int userId) {
        Iterable<Result<Item>> resources = minioRepository.getFilesInDirectory(path, userId, false);
        return resources.iterator().hasNext();
    }
}
