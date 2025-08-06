package com.makson.cloudfilestorage.integration.service;

import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.services.ResourceService;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
class ResourceServiceTest extends IntegrationTestBase {
    private final ResourceService resourceService;
    private final MinioClient minioClient;
    private final int USER_ID_TEST = 1;
    private final String TEST_FILE_NAME = "test.txt";
    @Value("${minio.bucket-name}")
    private String bucketName;

    @Test
    @WithUserDetails("testUserOne")
    @DisplayName("Uploading a file causes it to appear in a Minio bucket in the current user's root folder.")
    void uploadingFile_shouldAddFileToUserRootDirectory() {
        MultipartFile file = createTestFile();
        String path = PathUtil.getFullPathWithIdentificationDirectory("", getCurrentUser());

        resourceService.upload(path, List.of(file));
        ResourceResponseDto resourceResponseDto = Assertions.assertDoesNotThrow(() -> resourceService.getInfo(PathUtil.resolve(path, TEST_FILE_NAME)));
        Assertions.assertEquals(file.getSize(), resourceResponseDto.size());
    }

    @Test
    @WithUserDetails("testUserOne")
    @DisplayName("When you rename a file, it is renamed")
    void renamingFile_shouldRenameFile() {
        MultipartFile file = createTestFile();
        String path = PathUtil.getFullPathWithIdentificationDirectory("", getCurrentUser());
        resourceService.upload(path, List.of(file));

        String fromPath = PathUtil.resolve(path, TEST_FILE_NAME);
        String toPath = PathUtil.resolve(path, "to", TEST_FILE_NAME);

        ResourceResponseDto resourceResponseDto = Assertions.assertDoesNotThrow(() -> resourceService.renameOrMove(fromPath, toPath));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> resourceService.getInfo(fromPath));
        Assertions.assertDoesNotThrow(() -> resourceService.getInfo(toPath));
    }

    @Test
    @WithUserDetails("testUserOne")
    @DisplayName("When you rename a directory, it is renamed")
    void renamingDirectory_shouldRenameDirectory() {
        MultipartFile file = createTestFile();
        String path = PathUtil.getFullPathWithIdentificationDirectory("directory1/", getCurrentUser());
        resourceService.upload(path, List.of(file));

        String toPath = PathUtil.getFullPathWithIdentificationDirectory("directory2/", USER_ID_TEST);
        ResourceResponseDto resourceResponseDto = Assertions.assertDoesNotThrow(() -> resourceService.renameOrMove(path, toPath));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> resourceService.getInfo(path));
        Assertions.assertDoesNotThrow(() -> resourceService.getInfo(toPath));
    }

    @Test
    @WithUserDetails("testUserOne")
    @DisplayName("When you delete a file, it is deleted.")
    void deletingFile_shouldDeleteFile() {
        MultipartFile file = createTestFile();
        String path = PathUtil.getFullPathWithIdentificationDirectory("", getCurrentUser());
        resourceService.upload(path, List.of(file));

        resourceService.delete(path);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> resourceService.getInfo(PathUtil.resolve(path, TEST_FILE_NAME)));

    }

    @Test
    @WithUserDetails("testUserOne")
    @DisplayName("When you delete a directory, it is deleted.")
    void deletingDirectory_shouldDeleteDirectory() {
        MultipartFile file = createTestFile();
        String path = PathUtil.getFullPathWithIdentificationDirectory("directory/", getCurrentUser());
        resourceService.upload(path, List.of(file));

        resourceService.delete(path);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> resourceService.getInfo(path));
    }

    @Test
    @WithUserDetails("testUserOne")
    @DisplayName("The user should not have access to other people's files")
    void userShouldNotAccessTheOtherUsersFile() {
        MultipartFile file = createTestFile();
        String pathOne = PathUtil.getFullPathWithIdentificationDirectory("directoryUserOne/", getCurrentUser());
        resourceService.upload(pathOne, List.of(file));

        String pathTwo = PathUtil.getFullPathWithIdentificationDirectory("directoryUserTwo/", USER_ID_TEST);
        resourceService.upload(pathTwo, List.of(file));

        ResourceResponseDto userOneFile = resourceService.getInfo(PathUtil.resolve(pathOne, TEST_FILE_NAME));
        ResourceResponseDto userTwoFile = resourceService.getInfo(PathUtil.resolve(pathTwo, TEST_FILE_NAME));

        Assertions.assertNotEquals(userOneFile, userTwoFile);
    }

    @Test
    @WithUserDetails("testUserOne")
    @DisplayName("The user can find his own files, but not others")
    void userCanFindOwnFile_ButNotOther() {
        MultipartFile file = createTestFile();
        String pathOne = PathUtil.getFullPathWithIdentificationDirectory("directoryUserOne/", getCurrentUser());
        resourceService.upload(pathOne, List.of(file));

        String pathTwo = PathUtil.getFullPathWithIdentificationDirectory("directoryUserTwo/", USER_ID_TEST);
        resourceService.upload(pathTwo, List.of(file));

        var resourceUserOne = resourceService.search(getCurrentUser(), TEST_FILE_NAME);
        var resourceUserTwo = resourceService.search(User.builder().id(USER_ID_TEST).build(), TEST_FILE_NAME);

        Assertions.assertNotEquals(new HashSet<>(resourceUserOne), new HashSet<>(resourceUserTwo));
    }

    @AfterEach
    void cleanUp() throws Exception {
        List<DeleteObject> resources = new ArrayList<>();

        for (Result<Item> resource : getResourceInBucket()) {
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

    private MultipartFile createTestFile() {
        return new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "Hello World".getBytes());
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Iterable<Result<Item>> getResourceInBucket() {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .delimiter("/")
                        .recursive(true)
                        .build()
        );
    }
}