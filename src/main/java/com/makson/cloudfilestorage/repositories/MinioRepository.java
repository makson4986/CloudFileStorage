package com.makson.cloudfilestorage.repositories;

import com.makson.cloudfilestorage.exceptions.InternalMinioException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MinioRepository {
    @Value("${minio.bucket-name}")
    private String bucketName;
    @Value("${minio.user-root-directory}")
    private String USER_ROOT_DIRECTORY;
    private final MinioClient minioClient;

    public Optional<StatObjectResponse> getFileInfo(String path, int userId) {
        String fullPath = getFullPath(path, userId);

        try {
            return Optional.of(
                    minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(fullPath)
                                    .build())
            );
        } catch (ErrorResponseException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }

    public void deleteFile(String path, int userId) {
        String fullPath = getFullPath(path, userId);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .build());
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }

    public void uploadFile(String path, MultipartFile file, int userId) {
        String fullPath = getFullPath(path, userId);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }

    public Iterable<Result<Item>> getFilesInDirectory(String path, int userId, boolean isRecursive) {
        String fullPath = getFullPath(path, userId);

        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(fullPath)
                        .recursive(isRecursive)
                        .build()
        );
    }

    public void deleteDirectory(String path, int userId) {
        List<DeleteObject> resources = new ArrayList<>();

        try {
            for (Result<Item> resource : getFilesInDirectory(path, userId, true)) {
                resources.add(new DeleteObject(resource.get().objectName()));
            }

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(resources)
                    .build());

            for (Result<DeleteError> result : results) {
                result.get();
            }
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }

    private String getFullPath(String path, int userId) {
        return USER_ROOT_DIRECTORY.formatted(userId) + "/" + path;
    }
}
