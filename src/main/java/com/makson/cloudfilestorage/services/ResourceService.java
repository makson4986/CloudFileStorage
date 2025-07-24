package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.InternalMinioException;
import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.utils.PathUtil;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final FileService fileService;
    private final DirectoryService directoryService;

    public ResourceResponseDto getInfo(String path) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            return directoryService.getInfo(path);
        }

        return fileService.getInfo(path);
    }

    public void delete(String path) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            directoryService.delete(path);
        }

        fileService.delete(path);
    }

    public List<ResourceResponseDto> upload(String pathTo, List<MultipartFile> files) {
        List<ResourceResponseDto> result = new ArrayList<>();
        directoryService.createParentDirectories(PathUtil.getParent(pathTo));

        for (MultipartFile file : files) {
            String path = pathTo + file.getOriginalFilename();
            result.add(fileService.upload(path, file));
        }

        return result;
    }

    public InputStream download(String path) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            return directoryService.download(path);
        }

        return fileService.download(path);
    }

    public List<ResourceResponseDto> getContentsInfo(String path, boolean isRecursive) {
        var files = directoryService.getFilesInDirectory(path, isRecursive);
        List<ResourceResponseDto> result = new ArrayList<>();

        try {
            for (Result<Item> file : files) {
                String fileName = file.get().objectName();
                if (!fileName.equals(path)) {
                    result.add(getInfo(fileName));
                }
            }
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }

        //TODO tut

        return result;
    }

    public List<ResourceResponseDto> search(String query) {
        List<ResourceResponseDto> result = new ArrayList<>();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String path = PathUtil.getFullPathWithIdentificationDirectory("", user);
        for (ResourceResponseDto resource : getContentsInfo(path, true)) {
            if (resource.name().toLowerCase().contains(query.toLowerCase())) {
                result.add(resource);
            }
        }

        return result;
    }

    public ResourceResponseDto renameOrMove(String from, String to) {
        boolean isDirectory = directoryService.isDirectory(from);

        if (isDirectory) {
            return directoryService.renameOrMove(from, to);
        }

        return fileService.renameOrMove(from, to);
    }
}
