package com.hotel.application.dto.request;

import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * APPLICATION LAYER — DTO for PUT /api/rooms/{id}
 * Includes status so the frontend can toggle AVAILABLE ↔ OCCUPIED.
 */
public record UpdateRoomRequest(

        @NotBlank(message = "Le numéro de chambre est obligatoire")
        @Size(max = 10, message = "Le numéro de chambre ne peut pas dépasser 10 caractères")
        String number,

        @NotNull(message = "Le type de chambre est obligatoire")
        RoomType type,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
        @Digits(integer = 8, fraction = 2, message = "Format de prix invalide")
        BigDecimal price,

        @Min(value = 1, message = "La capacité doit être au minimum 1")
        @Max(value = 20, message = "La capacité ne peut pas dépasser 20 personnes")
        int capacity,

        @NotNull(message = "Le statut est obligatoire")
        RoomStatus status,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description
) {}