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

import java.io.IOException;
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

    private String getFullPath(UserDetails userDetails, String path) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        return String.format("user-%d-files/%s", user.getId(), path);
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
}
