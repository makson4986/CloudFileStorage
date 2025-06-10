package com.makson.cloudfilestorage.controllers;

import com.makson.cloudfilestorage.dto.AuthResponseDto;
import com.makson.cloudfilestorage.dto.ErrorDto;
import com.makson.cloudfilestorage.dto.UserDto;
import com.makson.cloudfilestorage.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @Operation(
            summary = "User registration",
            description = "Allows you to register a user",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "User registration successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Validation error",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDto.class))
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "User already exists",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDto.class))
                    ),
                    @ApiResponse(responseCode = "500",
                            description = "Unknown error",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDto.class))
                    )
            }
    )
    @PostMapping("/auth/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Validated UserDto userDto,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorDto(bindingResult));
        }

        var response = authService.signUp(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Login",
            description = "Allows you to login",
            responses = {
                    @ApiResponse(description = "Successful login",
                            responseCode = "200",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponseDto.class))
                    ),
                    @ApiResponse(description = "Validation error",
                            responseCode = "400",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDto.class))
                    ),
                    @ApiResponse(description = "Incorrect data",
                            responseCode = "401",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDto.class))
                    ),
                    @ApiResponse(description = "Unknow error",
                            responseCode = "500",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDto.class))
                    )
            }
    )
    @PostMapping("/auth/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Validated UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorDto(bindingResult));
        }

        AuthResponseDto response = authService.signIn(userDto);
        return ResponseEntity.ok(response);
    }

    private ErrorDto createErrorDto(BindingResult bindingResult) {
        return new ErrorDto(bindingResult.getFieldErrors().stream()
                .map(er -> er.getField() + ": " + er.getDefaultMessage())
                .toList()
                .getFirst()
        );
    }
}
