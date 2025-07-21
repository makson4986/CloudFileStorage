package com.makson.cloudfilestorage.dto;

import com.makson.cloudfilestorage.validation.Path;
import jakarta.validation.constraints.Size;

public record ResourceRequestDto(@Path @Size(min = 1, max = 64) String path) {

}