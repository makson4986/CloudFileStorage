package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import lombok.RequiredArgsConstructor;
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

    public ResourceResponseDto getInfo(String path, int userId) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            return directoryService.getInfo(path, userId);
        } else {
            return fileService.getInfo(path, userId);
        }
    }

    public void delete(String path, int userId) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            directoryService.delete(path, userId);
        } else {
            fileService.delete(path, userId);
        }
    }

    public List<ResourceResponseDto> upload(String pathTo, List<MultipartFile> files, int userId) {
        List<ResourceResponseDto> result = new ArrayList<>();

        for (MultipartFile file : files) {
            result.add(fileService.upload(pathTo, file, userId));
        }

        return result;
    }

    public InputStream download(String path, int userId) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            return directoryService.download(path, userId);
        } else {
            return fileService.download(path, userId);
        }
    }
}
