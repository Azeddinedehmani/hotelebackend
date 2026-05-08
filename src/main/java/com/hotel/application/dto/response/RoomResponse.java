package com.hotel.application.dto.response;

import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;

import java.math.BigDecimal;

/**
 * APPLICATION LAYER — Read-only view of a Room.
 * Constructed directly from the domain model — no mapper needed for the response.
 */
public record RoomResponse(
        Long       id,
        String     number,
        RoomType   type,
        BigDecimal price,
        int        capacity,
        RoomStatus status,
        String     description
) {
    /** Convenient factory — converts domain model → DTO. */
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getNumber(),
                room.getType(),
                room.getPrice(),
                room.getCapacity(),
                room.getStatus(),
                room.getDescription()
        );
    }
}