package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.Resource;
import com.makson.cloudfilestorage.dto.ResourceDto;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.models.User;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class ResourceService {
    @Value("${minio.bucket-name}")
    private String bucketName;
    private final MinioClient minioClient;
    private final UserService userService;

    public ResourceDto getInfo(String path, UserDetails userDetails) throws IOException, GeneralSecurityException, MinioException {
        String fullPath = getFullPath(userDetails, path);
        String resourceName = getResourceNameByPath(fullPath);
        boolean isFolder = isFolder(path);
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
        } else if (!isFolderExists(fullPath)) {
            throw new ResourceNotFoundException("Resource not found");
        }

        return new ResourceDto(
                getPathToResource(fullPath, resourceName),
                resourceName,
                isFolder ? null : size,
                isFolder ? Resource.DIRECTORY : Resource.FILE
        );
    }

    private String getFullPath(UserDetails userDetails, String path) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        return String.format("user-%d-files/%s", user.getId(), path);
    }

    private String getPathToResource(String fullPath, String resourceName) {
        final String userRootFolder = "^user-\\d+-files/";
        return fullPath.replaceFirst(userRootFolder, "").replace(resourceName, "");
    }

    private String getResourceNameByPath(String path) {
        var resources = path.split("(?<=/)");
        return resources[resources.length - 1];
    }

    private boolean isFolder(String path) {
        return path.lastIndexOf("/") == path.length() - 1;
    }

    private boolean isFolderExists(String fullPath) {
        Iterable<Result<Item>> folder = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(fullPath)
                        .maxKeys(1)
                        .build()
        );

        return folder.iterator().hasNext();
    }
}
