package com.hotel.application.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * APPLICATION LAYER — Request DTO for creating a Reservation.
 */
public record CreateReservationRequest(

        @NotNull(message = "L'identifiant du client est obligatoire")
        Long clientId,

        @NotNull(message = "L'identifiant de la chambre est obligatoire")
        Long roomId,

        @NotNull(message = "La date d'arrivée est obligatoire")
        LocalDate checkInDate,

        @NotNull(message = "La date de départ est obligatoire")
        LocalDate checkOutDate,

        @Min(value = 1, message = "Le nombre de personnes doit être au moins 1")
        int guests,

        String notes
) {}