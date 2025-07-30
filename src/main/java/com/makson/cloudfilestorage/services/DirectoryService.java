package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.InternalMinioException;
import com.makson.cloudfilestorage.exceptions.ResourceAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.ResourceDownloadException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.repositories.MinioRepository;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.GetObjectResponse;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
                    PathUtil.removeIdentificationDirectory(PathUtil.getParent(path)),
                    PathUtil.getName(path),
                    DIRECTORY_SIZE,
                    Resource.DIRECTORY
            );
        }

        throw new ResourceNotFoundException("Resource '%s' is not found".formatted(PathUtil.getName(path)));
    }

    public void delete(String path) {
        if (!isDirectoryExists(path)) {
            throw new ResourceNotFoundException("Resource '%s' is not found".formatted(PathUtil.getName(path)));
        }

        minioRepository.deleteDirectory(path);
    }

    public InputStream download(String path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(byteArrayOutputStream)) {
            if (!isDirectoryExists(path)) {
                throw new ResourceNotFoundException("Resource '%s' is not found".formatted(PathUtil.getName(path)));
            }

            for (var file : downloadFilesInDirectory(path)) {
                String resourceName = PathUtil.relativize(PathUtil.getParent(path), file.object());
                addResourceToZip(resourceName, zip, file);
            }
        } catch (IOException e) {
            throw new ResourceDownloadException("Error downloading resource. Please try again later", e);
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public ResourceResponseDto renameOrMove(String from, String to) {
        if (!isDirectoryExists(from)) {
            throw new ResourceNotFoundException("Resource '%s' is not found".formatted(PathUtil.getName(from)));
        }

        if (isDirectoryExists(to)) {
            throw new ResourceAlreadyExistException("Resource '%s' already exists".formatted(to));
        }

        for (Result<Item> resource : getFilesInDirectory(from, true)) {
            try {
                String fromPath = resource.get().objectName();
                String toPath = resource.get().objectName().replace(from, to);
                minioRepository.copy(fromPath, toPath);
            } catch (Exception e) {
                throw new InternalMinioException(e);
            }
        }

        minioRepository.deleteDirectory(from);
        return getInfo(to);

    }

    public void createParentDirectories(String path) {
        if (path.isBlank()) {
            return;
        }

        String[] partsPath = PathUtil.splitPath(path);
        String[] parentDirectories = Arrays.copyOfRange(partsPath, 0, partsPath.length);
        StringBuilder pathToParent = new StringBuilder();

        for (String parentDirectory : parentDirectories) {
            pathToParent.append(parentDirectory);
            minioRepository.createEmptyDirectory(pathToParent.toString());
        }
    }

    public Iterable<Result<Item>> getFilesInDirectory(String path, boolean isRecursive) {
        if (!isDirectoryExists(path)) {
            throw new ResourceNotFoundException("Resource '%s' is not found".formatted(PathUtil.getName(path)));
        }

        return minioRepository.getFilesInDirectory(path, isRecursive);
    }

    public void createIdentificationDirectory(String path) {
        minioRepository.createEmptyDirectory(path);
    }

    private boolean isDirectoryExists(String path) {
        return minioRepository.getFileInfo(path).isPresent();
    }

    private void addResourceToZip(String resourceName, ZipOutputStream zip, InputStream resourceContents) {
        byte[] buffer = new byte[8192];

        try (InputStream resource = resourceContents) {

            ZipEntry entry = new ZipEntry(resourceName);
            zip.putNextEntry(entry);

            if (!entry.isDirectory()) {
                int len;

                while ((len = resource.read(buffer)) > 0) {
                    zip.write(buffer, 0, len);
                }

                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new ResourceDownloadException("Error downloading resource. Please try again later", e);
        }
    }

    private List<GetObjectResponse> downloadFilesInDirectory(String path) {
        Iterable<Result<Item>> files = minioRepository.getFilesInDirectory(path, true);
        List<GetObjectResponse> result = new ArrayList<>();

        try {
            for (Result<Item> file : files) {
                result.add(minioRepository.download(file.get().objectName())
                        .orElseThrow(() -> new ResourceNotFoundException("Resource '%s' is not found".formatted(PathUtil.getName(path))))
                );
            }
        } catch (Exception e) {
            throw new ResourceDownloadException("Error downloading resource. Please try again later", e);
        }

        return result;
    }
}
