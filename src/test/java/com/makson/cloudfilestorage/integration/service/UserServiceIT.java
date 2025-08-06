package com.makson.cloudfilestorage.integration.service;

import com.makson.cloudfilestorage.models.User;
import com.makson.cloudfilestorage.services.UserService;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceIT extends IntegrationTestBase {
    private final UserService userService;

    @Test
    @Order(1)
    @DisplayName("Calling the 'save' method in the service responsible for working with users results in a new record appearing in the users table")
    void callingSaveUser_ShouldAddUserToTable() {
        User user = User.builder().username("username").password("password").build();
        userService.save(user);
        assertDoesNotThrow(() -> userService.loadUserByUsername(user.getUsername()));
    }

    @Test
    @Order(2)
    @DisplayName("Creating a user with a non-unique username results in the expected exception type")
    void creatingUserWithNonUniqueUsername_ShouldThrowException() {
        User user = User.builder().username("username").password("password").build();
        Exception exception = assertThrows(Exception.class, () -> userService.save(user));
        assertInstanceOf(ConstraintViolationException.class, exception.getCause());
    }
}