package com.makson.cloudfilestorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.nio.file.Path;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponseDto(
        String path,
        String name,
        Long size,
        Resource type) {
}
