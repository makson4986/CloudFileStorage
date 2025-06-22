package com.makson.cloudfilestorage.validation.impl;

import com.makson.cloudfilestorage.validation.Path;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PathValidator implements ConstraintValidator<Path, String> {
    private final String CORRECT_PATH = "^([a-zA-Z0-9._-]+/)*([a-zA-Z0-9._-]+\\.[a-zA-Z0-9]+/?|)$\n";

    @Override
    public boolean isValid(String  value, ConstraintValidatorContext context) {
        return value.matches(CORRECT_PATH);
    }
}
