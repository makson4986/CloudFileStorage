package com.makson.cloudfilestorage.services;

import com.makson.cloudfilestorage.dto.UserRequestDto;
import com.makson.cloudfilestorage.dto.UserResponseDto;
import com.makson.cloudfilestorage.exceptions.DataBaseException;
import com.makson.cloudfilestorage.exceptions.UserAlreadyExistException;
import com.makson.cloudfilestorage.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final CookieClearingLogoutHandler cookieClearingLogoutHandler;
    private final SecurityContextLogoutHandler securityContextLogoutHandler;
    private final UserService userService;
    private final DirectoryService directoryService;

    public UserResponseDto signUp(UserRequestDto userRequestDto, HttpServletRequest request, HttpServletResponse response) {
        User user = User.builder()
                .username(userRequestDto.username())
                .password(passwordEncoder.encode(userRequestDto.password()))
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

        signIn(userRequestDto, request, response);
        directoryService.createIdentificationDirectory("user-%s-files/".formatted(registeredUser.getId()));
        return new UserResponseDto(registeredUser.getUsername());
    }

    public UserResponseDto signIn(UserRequestDto userRequestDto, HttpServletRequest request, HttpServletResponse response) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userRequestDto.username(), userRequestDto.password()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticate);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);

        UserDetails user = (UserDetails) authenticate.getPrincipal();
        return new UserResponseDto(user.getUsername());
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cookieClearingLogoutHandler.logout(request, response, authentication);
        securityContextLogoutHandler.logout(request, response, authentication);
    }
}
