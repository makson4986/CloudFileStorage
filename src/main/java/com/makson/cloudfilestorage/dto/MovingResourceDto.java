package com.makson.cloudfilestorage.dto;

import com.makson.cloudfilestorage.validation.Path;

public record MovingResourceDto(
        @Path String from,
        @Path String to
) { }
