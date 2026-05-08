package com.hotel.application.dto.request;

import com.hotel.domain.model.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * APPLICATION LAYER — DTO for POST /api/rooms
 * Bean Validation keeps bad data out before it reaches the domain.
 */
public record CreateRoomRequest(

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

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description
) {}