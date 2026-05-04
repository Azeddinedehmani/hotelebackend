package com.hotel.application.dto.response;

import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;

import java.time.LocalDateTime;

/**
 * APPLICATION LAYER — Outbound DTO for User data.
 * Never exposes the password.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
    /**
     * Static factory from domain model.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}