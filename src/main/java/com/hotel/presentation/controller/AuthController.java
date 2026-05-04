package com.hotel.presentation.controller;

import com.hotel.application.dto.request.LoginRequest;
import com.hotel.application.dto.request.RegisterRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.AuthResponse;
import com.hotel.application.dto.response.UserResponse;
import com.hotel.application.usecase.AuthUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * PRESENTATION LAYER — Authentication REST controller.
 * Routes: POST /api/auth/register, POST /api/auth/login, GET /api/auth/me
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    /**
     * POST /api/auth/register
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authUseCase.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie", authResponse));
    }

    /**
     * POST /api/auth/login
     * Authenticate and receive JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authUseCase.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", authResponse));
    }

    /**
     * GET /api/auth/me
     * Get current authenticated user profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponse user = authUseCase.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}