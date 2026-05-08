package com.hotel.application.dto.response;

import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * APPLICATION LAYER — Read-only view of a Reservation  (v2 — real timestamps)
 */
public record ReservationResponse(
        Long              id,
        Long              clientId,
        Long              roomId,
        LocalDate         checkInDate,
        LocalDate         checkOutDate,
        ReservationStatus status,
        int               guests,
        String            notes,
        long              durationNights,
        LocalDateTime     actualCheckInAt,
        LocalDateTime     actualCheckOutAt,
        ClientResponse    client,
        RoomResponse      room
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(), r.getClientId(), r.getRoomId(),
                r.getCheckInDate(), r.getCheckOutDate(),
                r.getStatus(), r.getGuests(), r.getNotes(),
                r.getDurationNights(),
                r.getActualCheckInAt(), r.getActualCheckOutAt(),
                null, null
        );
    }

    public static ReservationResponse from(Reservation r, ClientResponse client, RoomResponse room) {
        return new ReservationResponse(
                r.getId(), r.getClientId(), r.getRoomId(),
                r.getCheckInDate(), r.getCheckOutDate(),
                r.getStatus(), r.getGuests(), r.getNotes(),
                r.getDurationNights(),
                r.getActualCheckInAt(), r.getActualCheckOutAt(),
                client, room
        );
    }
}