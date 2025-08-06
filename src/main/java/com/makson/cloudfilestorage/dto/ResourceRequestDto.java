package com.makson.cloudfilestorage.dto;

import com.makson.cloudfilestorage.validation.Path;
import com.makson.cloudfilestorage.validation.groups.PathNotBlankCheck;
import jakarta.validation.constraints.NotBlank;

public record ResourceRequestDto(
        @Path
        @NotBlank(groups = PathNotBlankCheck.class)
        String path) {
}