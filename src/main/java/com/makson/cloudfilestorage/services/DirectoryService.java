package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.repositories.MinioRepository;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final MinioRepository minioRepository;
    private final Long DIRECTORY_SIZE = null;

    public ResourceResponseDto createEmpty(String path) {
        String parentDirectory = PathUtil.getParent(path);
        String name = PathUtil.getName(path);

        if (!isDirectoryExists(parentDirectory)) {
            throw new ResourceNotFoundException("Parent directory does not exist");
        }

        if (isDirectoryExists(path)) {
            throw new ResourceAlreadyExistException("Directory '%s' already exists".formatted(name));
        }

        minioRepository.createEmptyDirectory(path);

        return new ResourceResponseDto(
                parentDirectory.replaceFirst("user-\\d+-files/", ""),
                name,
                DIRECTORY_SIZE,
                Resource.DIRECTORY
        );
    }

    public ResourceResponseDto getInfo(String path) {
        if (isDirectoryExists(path)) {
            return new ResourceResponseDto(
                    PathUtil.getParent(path).replaceFirst("user-\\d+-files/", ""),
                    PathUtil.getName(path),
                    DIRECTORY_SIZE,
                    Resource.DIRECTORY
            );
        }

        throw new ResourceNotFoundException("Resource not found");
    }

    public void delete(String path) {
        if (!isDirectoryExists(path)) {
            throw new ResourceNotFoundException("Resource not found");
        }

        minioRepository.deleteDirectory(path);
    }

    public InputStream download(String path) {
        return null;
    }

    public void createParentDirectories(String path) {
        String[] partsPath = PathUtil.splitPath(path);
        String[] parentDirectories = Arrays.copyOfRange(partsPath, 0, partsPath.length - 1);
        StringBuilder pathToParent = new StringBuilder();

        for (String parentDirectory : parentDirectories) {
            pathToParent.append(parentDirectory);
            minioRepository.createEmptyDirectory(pathToParent.toString());
        }
    }

    public Iterable<Result<Item>> getFilesInDirectory(String path) {
        if (!isDirectoryExists(path)) {
            throw new ResourceNotFoundException("Resource not found");
        }

        return minioRepository.getFilesInDirectory(path, false);
    }

    public boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    private boolean isDirectoryExists(String path) {
        return minioRepository.getFileInfo(path).isPresent();
    }
}
