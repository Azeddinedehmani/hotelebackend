package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when a room is not available for the requested dates.
 */
public class RoomNotAvailableException extends DomainException {

    public RoomNotAvailableException(Long roomId, String checkIn, String checkOut) {
        super("La chambre #" + roomId + " n'est pas disponible du " + checkIn + " au " + checkOut);
    }
}