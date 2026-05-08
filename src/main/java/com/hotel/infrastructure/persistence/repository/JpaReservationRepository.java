package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.ReservationStatus;
import com.hotel.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository for ReservationEntity.
 */
public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByClientId(Long clientId);

    List<ReservationEntity> findByRoomId(Long roomId);

    List<ReservationEntity> findByStatus(ReservationStatus status);

    /**
     * Returns true if the room has any non-cancelled reservation that overlaps
     * the given date range.
     *
     * Overlap condition: existing.checkIn < requested.checkOut
     *                AND existing.checkOut > requested.checkIn
     *
     * @param excludeId  id to exclude from the check (for update use-case); pass null to check all
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM ReservationEntity r
            WHERE r.roomId = :roomId
              AND r.status NOT IN ('CANCELLED', 'CHECKED_OUT')
              AND r.checkInDate  < :checkOut
              AND r.checkOutDate > :checkIn
              AND (:excludeId IS NULL OR r.id <> :excludeId)
            """)
    boolean existsOverlap(@Param("roomId")    Long roomId,
                          @Param("checkIn")   LocalDate checkIn,
                          @Param("checkOut")  LocalDate checkOut,
                          @Param("excludeId") Long excludeId);
}