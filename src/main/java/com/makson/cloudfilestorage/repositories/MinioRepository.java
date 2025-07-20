package com.makson.cloudfilestorage.repositories;

import com.makson.cloudfilestorage.exceptions.InternalMinioException;
import com.makson.cloudfilestorage.exceptions.ResourceDownloadException;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MinioRepository {
    @Value("${minio.bucket-name}")
    private String bucketName;
    private final MinioClient minioClient;

    public Optional<StatObjectResponse> getFileInfo(String path) {
        try {
            return Optional.of(
                    minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(path)
                                    .build())
            );
        } catch (ErrorResponseException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }

    public void deleteFile(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }

    public void uploadFile(String path, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }

    public Iterable<Result<Item>> getFilesInDirectory(String path, boolean isRecursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .delimiter("/")
                        .recursive(isRecursive)
                        .build()
        );
    }

    public List<GetObjectResponse> downloadFilesInDirectory(String path) {
        Iterable<Result<Item>> files = getFilesInDirectory(path, true);
        List<GetObjectResponse> result = new ArrayList<>();

        try {
            for (Result<Item> file : files) {
                result.add(download(file.get().objectName())
                        .orElseThrow(() -> new ResourceNotFoundException("Resource not found"))
                );
            }
        } catch (Exception e) {
            throw new ResourceDownloadException("Error downloading resource. Please try again later", e);
        }

        return result;
    }

    public void deleteDirectory(String path) {
        List<DeleteObject> resources = new ArrayList<>();

        try {
            for (Result<Item> resource : getFilesInDirectory(path, true)) {
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

    public Optional<GetObjectResponse> download(String path) {
        try {
            return Optional.of(
                    minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build())
            );
        } catch (ErrorResponseException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new ResourceDownloadException("Error downloading resource. Please try again later", e);
        }
    }

    public void createEmptyDirectory(String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }
    }
}
