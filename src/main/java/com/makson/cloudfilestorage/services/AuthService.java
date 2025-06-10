package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.UserDto;
import com.makson.cloudfilestorage.dto.AuthResponseDto;
import com.makson.cloudfilestorage.exceptions.DataBaseException;
import com.makson.cloudfilestorage.exceptions.UserAlreadyExistException;
import com.makson.cloudfilestorage.models.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
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

    public AuthResponseDto signIn(UserDto userDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.username(), userDto.password()));
        UserDetails user = (UserDetails) authenticate.getPrincipal();
        return new AuthResponseDto(user.getUsername());
    }
}
