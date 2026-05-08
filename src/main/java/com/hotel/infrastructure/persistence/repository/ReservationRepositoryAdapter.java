package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;
import com.hotel.domain.repository.ReservationRepository;
import com.hotel.infrastructure.persistence.mapper.ReservationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adapter implementing domain's ReservationRepository port.
 * Pont entre le domaine et JPA.
 * Le domaine dépend de l'interface ; Spring injecte cette implémentation.
 */
@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;
    private final ReservationEntityMapper  mapper;

    @Override
    public Reservation save(Reservation reservation) {
        var entity = mapper.toEntity(reservation);
        var saved  = jpaReservationRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaReservationRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByClientId(Long clientId) {
        return jpaReservationRepository.findByClientId(clientId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByRoomId(Long roomId) {
        return jpaReservationRepository.findByRoomId(roomId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return jpaReservationRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasOverlappingReservation(Long roomId, LocalDate checkIn,
                                              LocalDate checkOut, Long excludeId) {
        return jpaReservationRepository.existsOverlap(roomId, checkIn, checkOut, excludeId);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaReservationRepository.count();
    }
}