package com.hotel.application.dto.response;

import com.hotel.domain.model.Role;

/**
 * APPLICATION LAYER — Outbound DTO returned after authentication.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String token, Long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}