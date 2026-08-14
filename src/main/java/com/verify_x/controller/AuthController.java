package com.verify_x.controller;

import com.verify_x.dto.*;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AuthController {

    private final AuthService authService;


    // =========================================================
    // CANDIDATE REGISTRATION
    // =========================================================

    @PostMapping("/candidateRegister")
    public ResponseEntity<ApiResponse<String>> registerUser(

            @Valid
            @ParameterObject
            @ModelAttribute
            UserRegistrationDto registrationDto) {

        String response = authService.register(registrationDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<String>builder()
                                .success(true)
                                .message("User registered successfully.")
                                .data(response)
                                .build()
                );
    }


    // =========================================================
    // CANDIDATE LOGIN
    // =========================================================

    @PostMapping("/candidateLogin")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(

            @Valid
            @RequestBody
            LoginRequestDto loginRequestDto) {

        LoginResponseDto response =
                authService.login(loginRequestDto);

        return ResponseEntity.ok(
                ApiResponse.<LoginResponseDto>builder()
                        .success(true)
                        .message("Login successful.")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserDto>> currentUser() {

        return ResponseEntity.ok(
                ApiResponse.<CurrentUserDto>builder()
                        .success(true)
                        .message("Current user fetched successfully.")
                        .data(authService.getCurrentUser())
                        .build()
        );
    }


    // =========================================================
    // VERIFY REGISTRATION OTP
    // =========================================================

    @PostMapping("/verify-registration-otp")
    public ResponseEntity<ApiResponse<String>> verifyRegistrationOtp(

            @Valid
            @RequestBody
            VerifyOtpRequestDto request) {

        String response =
                authService.verifyRegistrationOtp(request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("OTP verification successful.")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // CANDIDATE LOGOUT
    // =========================================================

    @PostMapping("/candidateLogout")
    public ResponseEntity<ApiResponse<String>> logout(

            @RequestHeader("Authorization")
            String authHeader) {

        String token =
                authHeader.replace("Bearer ", "");

        authService.logout(token);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Logout successful.")
                        .data("Token invalidated successfully.")
                        .build()
        );
    }


    // =========================================================
    // HR LOGIN
    // =========================================================

    @PostMapping("/hrLogin")
    public ResponseEntity<ApiResponse<HrLoginResponseDto>> adminLogin(

            @Valid
            @RequestBody
            AdminLoginRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.<HrLoginResponseDto>builder()
                        .success(true)
                        .message("Admin login successful.")
                        .data(authService.adminLogin(request))
                        .build()
        );
    }


    // =========================================================
    // HR LOGOUT
    // =========================================================

    @PostMapping("/hrLogout")
    public ResponseEntity<ApiResponse<String>> adminLogout(

            @RequestHeader("Authorization")
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new BadCredentialsException(
                    "Invalid Authorization header."
            );
        }

        String token =
                authHeader.substring(7);

        authService.adminLogout(token);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Admin logged out successfully.")
                        .data("Logout successful.")
                        .build()
        );
    }
}