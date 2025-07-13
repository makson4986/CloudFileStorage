package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.ResourceResponseDto;
import com.makson.cloudfilestorage.exceptions.InternalMinioException;
import io.minio.Result;
import io.minio.messages.Item;
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

    public ResourceResponseDto getInfo(String path) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            return directoryService.getInfo(path);
        } else {
            return fileService.getInfo(path);
        }
    }

    public void delete(String path) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            directoryService.delete(path);
        } else {
            fileService.delete(path);
        }
    }

    public List<ResourceResponseDto> upload(String pathTo, List<MultipartFile> files) {
        List<ResourceResponseDto> result = new ArrayList<>();

        for (MultipartFile file : files) {
            String path = pathTo + file.getOriginalFilename();
            directoryService.createParentDirectories(path);
            result.add(fileService.upload(path, file));
        }

        return result;
    }

    public InputStream download(String path) {
        boolean isDirectory = directoryService.isDirectory(path);

        if (isDirectory) {
            return directoryService.download(path);
        } else {
            return fileService.download(path);
        }
    }

    public List<ResourceResponseDto> getContentsInfo(String path) {
        var files = directoryService.getFilesInDirectory(path);
        List<ResourceResponseDto> result = new ArrayList<>();

        try {
            for (Result<Item> file : files) {
                String fileName = file.get().objectName();
                if(!fileName.equals(path)) {
                    result.add(getInfo(fileName));
                }
            }
        } catch (Exception e) {
            throw new InternalMinioException(e);
        }

        return result;
    }
}
