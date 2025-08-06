package com.makson.cloudfilestorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponseDto(
        String path,
        String name,
        Long size,
        Resource type) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ResourceResponseDto that = (ResourceResponseDto) o;
        return Objects.equals(size, that.size) && Objects.equals(path, that.path) && Objects.equals(name, that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name, size, type);
    }
}
