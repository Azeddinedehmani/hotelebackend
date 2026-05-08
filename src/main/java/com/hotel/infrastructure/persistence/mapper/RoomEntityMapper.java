package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.Room;
import com.hotel.infrastructure.persistence.entity.RoomEntity;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — Bidirectional mapper between domain Room and JPA RoomEntity.
 * Keeps domain and persistence completely decoupled.
 */
@Component
public class RoomEntityMapper {

    /**
     * JPA entity → domain model (reconstitution depuis la BDD).
     */
    public Room toDomain(RoomEntity entity) {
        if (entity == null) return null;
        return Room.reconstitute(
                entity.getId(),
                entity.getNumber(),
                entity.getType(),
                entity.getPrice(),
                entity.getCapacity(),
                entity.getStatus(),
                entity.getDescription()
        );
    }

    /**
     * Domain model → JPA entity (pour persistance).
     */
    public RoomEntity toEntity(Room room) {
        if (room == null) return null;
        return RoomEntity.builder()
                .id(room.getId())
                .number(room.getNumber())
                .type(room.getType())
                .price(room.getPrice())
                .capacity(room.getCapacity())
                .status(room.getStatus())
                .description(room.getDescription())
                .build();
    }
}