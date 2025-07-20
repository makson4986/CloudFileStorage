package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.repositories.MinioRepository;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioRepository minioRepository;

    public ResourceResponseDto getInfo(String path) {
        Optional<StatObjectResponse> fileInfo = minioRepository.getFileInfo(path);

        if (fileInfo.isPresent()) {
            return new ResourceResponseDto(
                    PathUtil.removeIdentificationDirectory(PathUtil.getParent(path)),
                    PathUtil.getName(path),
                    fileInfo.get().size(),
                    Resource.FILE
            );
        }

        throw new ResourceNotFoundException("Resource not found");
    }

    public void delete(String path) {
        if (!isFileExists(path)) {
            throw new ResourceNotFoundException("Resource not found");

        }

        minioRepository.deleteFile(path);
    }

    public ResourceResponseDto upload(String path, MultipartFile file) {
        if (isFileExists(path)) {
            throw new ResourceAlreadyExistException("File '%s' already exists".formatted(file.getOriginalFilename()));
        }

        minioRepository.uploadFile(path, file);
        return getInfo(path);
    }

    public InputStream download(String path) {
        var downloadedFile = minioRepository.download(path);
        return downloadedFile.orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }

    public boolean isFileExists(String path) {
        return minioRepository.getFileInfo(path).isPresent();
    }
}
