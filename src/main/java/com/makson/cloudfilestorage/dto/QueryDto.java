package com.makson.cloudfilestorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QueryDto(@NotBlank @Size(min = 1, max = 64) String query) {
}
