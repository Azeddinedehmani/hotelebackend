package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateRoomRequest;
import com.hotel.application.dto.request.UpdateRoomRequest;
import com.hotel.application.dto.response.RoomResponse;
import com.hotel.domain.exception.RoomAlreadyExistsException;
import com.hotel.domain.exception.RoomNotFoundException;
import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * APPLICATION LAYER — RoomUseCase Implementation.
 * Orchestrates domain objects and repository without any framework concern.
 */
@Service
@Transactional
public class RoomUseCaseImpl implements RoomUseCase {

    private static final Logger log = LoggerFactory.getLogger(RoomUseCaseImpl.class);

    private final RoomRepository roomRepository;

    public RoomUseCaseImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // ────────────────── Queries ──────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByType(RoomType type) {
        return roomRepository.findAllByType(type)
                .stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        return RoomResponse.from(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findAllAvailable()
                .stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRoomsByType(RoomType type) {
        return roomRepository.findAvailableByType(type)
                .stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return roomRepository.findAvailableForDates(checkIn, checkOut)
                .stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRoomsForDatesByType(LocalDate checkIn,
                                                               LocalDate checkOut,
                                                               RoomType type) {
        validateDates(checkIn, checkOut);
        return roomRepository.findAvailableForDatesByType(checkIn, checkOut, type)
                .stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    // ────────────────── Commands ──────────────────

    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (roomRepository.existsByNumber(request.number())) {
            throw new RoomAlreadyExistsException(request.number());
        }

        Room room = Room.create(
                request.number(),
                request.type(),
                request.price(),
                request.capacity(),
                request.description()
        );

        Room saved = roomRepository.save(room);
        log.info("Chambre créée — id={}, number={}", saved.getId(), saved.getNumber());
        return RoomResponse.from(saved);
    }

    @Override
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        Room room = findOrThrow(id);

        // Check number uniqueness only if it changed
        if (!room.getNumber().equalsIgnoreCase(request.number())
                && roomRepository.existsByNumber(request.number())) {
            throw new RoomAlreadyExistsException(request.number());
        }

        room.updateDetails(
                request.number(),
                request.type(),
                request.price(),
                request.capacity(),
                request.description()
        );

        // Apply status change via domain behaviour
        if (request.status() == RoomStatus.OCCUPIED && room.isAvailable()) {
            room.markAsOccupied();
        } else if (request.status() == RoomStatus.AVAILABLE && !room.isAvailable()) {
            room.markAsAvailable();
        }

        Room updated = roomRepository.save(room);
        log.info("Chambre mise à jour — id={}", updated.getId());
        return RoomResponse.from(updated);
    }

    @Override
    public void deleteRoom(Long id) {
        findOrThrow(id);
        roomRepository.deleteById(id);
        log.info("Chambre supprimée — id={}", id);
    }

    // ────────────────── Helpers ──────────────────

    private Room findOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null)
            throw new IllegalArgumentException("Les dates d'arrivée et de départ sont obligatoires");
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("La date de départ doit être après la date d'arrivée");
    }
}