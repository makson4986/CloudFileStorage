package com.makson.cloudfilestorage.validation;

import com.makson.cloudfilestorage.validation.impl.PathValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PathValidator.class)
@Documented
public @interface Path {
    String message() default "Incorrect path to resource";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
