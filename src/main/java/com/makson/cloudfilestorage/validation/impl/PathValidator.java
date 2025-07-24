package com.makson.cloudfilestorage.validation.impl;

import com.makson.cloudfilestorage.utils.PathUtil;
import com.makson.cloudfilestorage.validation.Path;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PathValidator implements ConstraintValidator<Path, String> {
    private final String CORRECT_PATH = "^([a-zA-Zа-яА-ЯёЁ0-9._\\- ]+/)*([a-zA-Zа-яА-ЯёЁ0-9._\\- ]+\\.[a-zA-Zа-яА-ЯёЁ0-9]+/?)?$";

    @Override
    public boolean isValid(String  value, ConstraintValidatorContext context) {
        return value.matches(CORRECT_PATH) && PathUtil.getName(value).length() < 64;
    }
}
