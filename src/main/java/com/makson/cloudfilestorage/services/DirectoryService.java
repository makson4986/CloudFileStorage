package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceAlreadyExistException;
import com.makson.cloudfilestorage.exceptions.ResourceDownloadException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.repositories.MinioRepository;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.GetObjectResponse;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
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

        throw new ResourceNotFoundException("Resource not found");
    }

    public void delete(String path) {
        if (!isDirectoryExists(path)) {
            throw new ResourceNotFoundException("Resource not found");
        }

        minioRepository.deleteDirectory(path);
    }

    public InputStream download(String path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(byteArrayOutputStream)) {
            if (!isDirectoryExists(path)) {
                throw new ResourceNotFoundException("Resource not found");
            }

            for (var file : minioRepository.downloadFilesInDirectory(path)) {
                String resourceName = PathUtil.relativize(PathUtil.getParent(path), file.object());
                addResourceToZip(resourceName, zip, file);
            }
        } catch (IOException e) {
            throw new ResourceDownloadException("Error downloading resource. Please try again later", e);
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
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
}
