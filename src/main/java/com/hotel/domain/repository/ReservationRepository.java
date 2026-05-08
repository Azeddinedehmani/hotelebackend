package com.hotel.domain.repository;

import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — ReservationRepository Port
 * Interface définie dans le domaine ; implémentée en infrastructure.
 * Le domaine dépend de cette abstraction, PAS de JPA/Hibernate.
 */
public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    /** All reservations for a given client */
    List<Reservation> findByClientId(Long clientId);

    /** All reservations for a given room */
    List<Reservation> findByRoomId(Long roomId);

    /** All reservations filtered by status */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Check if a room has an active (non-cancelled) reservation overlapping
     * the given date range. Used to prevent double-booking.
     *
     * @param roomId     the room to check
     * @param checkIn    start of the requested period
     * @param checkOut   end of the requested period
     * @param excludeId  reservation id to exclude from the check (for updates); null to include all
     */
    boolean hasOverlappingReservation(Long roomId, LocalDate checkIn,
                                      LocalDate checkOut, Long excludeId);

    void deleteById(Long id);

    long count();
}