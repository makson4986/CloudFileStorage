package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.models.User;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {
    @Value("${minio.bucket-name}")
    private String bucketName;
    private final MinioClient minioClient;
    private final UserService userService;

    public ResourceResponseDto getInfo(String path, UserDetails userDetails) throws IOException, GeneralSecurityException, MinioException {
        String fullPath = getFullPath(userDetails, path);
        String resourceName = getResourceNameByPath(fullPath);
        boolean isFolder = isDirectory(path);
        long size = 0;

        if (!isFolder) {
            try {
                StatObjectResponse resourceInfo = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fullPath)
                                .build());
                size = resourceInfo.size();
            } catch (ErrorResponseException e) {
                throw new ResourceNotFoundException("Resource not found");
            }
        } else if (!isDirectoryExists(fullPath)) {
            throw new ResourceNotFoundException("Resource not found");
        }

        return new ResourceResponseDto(
                getPathToParentDirectory(fullPath, resourceName),
                resourceName,
                isFolder ? null : size,
                isFolder ? Resource.DIRECTORY : Resource.FILE
        );
    }

    public void delete(String path, UserDetails userDetails) throws IOException, GeneralSecurityException, MinioException {
        String fullPath = getFullPath(userDetails, path);
        boolean isDirectory = isDirectory(path);

        if (isDirectory) {
            deleteDirectory(fullPath);
        } else {
            deleteFile(fullPath);
        }
    }

    public OutputStream download(String path, UserDetails userDetails) throws IOException, GeneralSecurityException, MinioException {
        String fullPath = getFullPath(userDetails, path);
        boolean isDirectory = isDirectory(fullPath);

        if (isDirectory) {
            downloadDirectory(fullPath);
        } else {
            downloadFile(fullPath);
        }
        return null;
    }

    public List<ResourceResponseDto> upload(String pathToParentDirectory, UserDetails userDetails, List<MultipartFile> files) throws IOException, GeneralSecurityException, MinioException {
        for (MultipartFile file : files) {
            String fullPath = getFullPath(pathToParentDirectory, file.getOriginalFilename(), userDetails);

            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullPath)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        }

        return null;
    }

    private String getFullPath(UserDetails userDetails, String path) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        return String.format("user-%d-files/%s", user.getId(), path);
    }

    private String getFullPath(String pathToParentDirectory, String resourceName, UserDetails userDetails) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        return String.format("user-%d-files/%s%s", user.getId(), pathToParentDirectory, resourceName);
    }

    private String getPathToParentDirectory(String fullPath, String resourceName) {
        final String userRootFolder = "^user-\\d+-files/";
        return fullPath.replaceFirst(userRootFolder, "").replace(resourceName, "");
    }

    private String getResourceNameByPath(String path) {
        var resources = path.split("(?<=/)");
        return resources[resources.length - 1];
    }

    private boolean isDirectory(String path) {
        return path.lastIndexOf("/") == path.length() - 1;
    }

    private boolean isDirectoryExists(String fullPath) {
        Iterable<Result<Item>> resources = getListResources(fullPath);
        return resources.iterator().hasNext();
    }

    private Iterable<Result<Item>> getListResources(String prefix) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );
    }

    private void deleteFile(String path) throws IOException, GeneralSecurityException, MinioException {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }

    private void deleteDirectory(String path) throws IOException, GeneralSecurityException, MinioException {
        List<DeleteObject> resources = new ArrayList<>();

        if (!isDirectoryExists(path)) {
            throw new ResourceNotFoundException("Resource not found");
        }

        for (Result<Item> resource : getListResources(path)) {
            resources.add(new DeleteObject(resource.get().objectName()));
        }

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(resources)
                .build());

        for (Result<DeleteError> result : results) {
            result.get();
        }
    }

    private OutputStream downloadFile(String path) throws IOException, GeneralSecurityException, MinioException {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build())) {

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int bytesRead;

            while ((bytesRead = stream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            return buffer;
        }  catch (ErrorResponseException e) {
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    private void downloadDirectory(String path) throws IOException, GeneralSecurityException, MinioException {

    }
}
