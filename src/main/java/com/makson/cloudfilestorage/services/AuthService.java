package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.UserDto;
import com.makson.cloudfilestorage.dto.AuthResponseDto;
import com.makson.cloudfilestorage.exceptions.DataBaseException;
import com.makson.cloudfilestorage.exceptions.UserAlreadyExistException;
import com.makson.cloudfilestorage.models.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthResponseDto signUp(UserDto userDto) {
        User user = User.builder()
                .username(userDto.username())
                .password(passwordEncoder.encode(userDto.password()))
                .build();

        User registeredUser;
        try {
            registeredUser = userService.save(user);
        } catch (Exception e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new UserAlreadyExistException("User already exists");
            }
            throw new DataBaseException(e);
        }


        return new AuthResponseDto(registeredUser.getUsername());
    }

    public void signIn() {

    }
}
