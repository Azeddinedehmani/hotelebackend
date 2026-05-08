package com.hotel.presentation.controller;

import com.hotel.application.dto.request.CreateRoomRequest;
import com.hotel.application.dto.request.UpdateRoomRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.RoomResponse;
import com.hotel.application.usecase.RoomUseCase;
import com.hotel.domain.model.RoomType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * PRESENTATION LAYER — Room REST Controller.
 *
 *  GET    /api/rooms                           → toutes les chambres (ADMIN, RECEPTIONNISTE)
 *  GET    /api/rooms/{id}                      → une chambre par id  (ADMIN, RECEPTIONNISTE, CLIENT)
 *  GET    /api/rooms/available                 → chambres disponibles (ADMIN, RECEPTIONNISTE, CLIENT)
 *  GET    /api/rooms/available?checkIn=&checkOut= → disponibilité par dates
 *  POST   /api/rooms                           → créer une chambre   (ADMIN)
 *  PUT    /api/rooms/{id}                      → modifier une chambre (ADMIN)
 *  DELETE /api/rooms/{id}                      → supprimer une chambre (ADMIN)
 *
 * CORRECTION : CLIENT doit pouvoir lire /rooms/available et /rooms/{id}
 * pour afficher les chambres dans RoomsPage et BookingPage.
 * Seul GET /rooms (liste admin complète) reste réservé à ADMIN/RECEPTIONNISTE.
 */
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomUseCase roomUseCase;

    // ──────────────────────────── GET ALL ────────────────────────────

    /**
     * GET /api/rooms
     * GET /api/rooms?type=DOUBLE
     * Réservé à l'admin et la réception (liste complète avec tous les statuts).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms(
            @RequestParam(required = false) RoomType type) {

        List<RoomResponse> rooms = (type != null)
                ? roomUseCase.getRoomsByType(type)
                : roomUseCase.getAllRooms();

        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    // ──────────────────────────── GET AVAILABLE ────────────────────────────

    /**
     * GET /api/rooms/available
     * GET /api/rooms/available?type=SUITE
     * GET /api/rooms/available?checkIn=2025-06-01&checkOut=2025-06-05
     * GET /api/rooms/available?checkIn=2025-06-01&checkOut=2025-06-05&type=SUITE
     *
     * ✅ CORRECTION : CLIENT ajouté — nécessaire pour que RoomsPage fonctionne.
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) RoomType type) {

        List<RoomResponse> rooms;

        if (checkIn != null && checkOut != null && type != null) {
            rooms = roomUseCase.getAvailableRoomsForDatesByType(checkIn, checkOut, type);
        } else if (checkIn != null && checkOut != null) {
            rooms = roomUseCase.getAvailableRoomsForDates(checkIn, checkOut);
        } else if (type != null) {
            rooms = roomUseCase.getAvailableRoomsByType(type);
        } else {
            rooms = roomUseCase.getAvailableRooms();
        }

        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    // ──────────────────────────── GET BY ID ────────────────────────────

    /**
     * GET /api/rooms/{id}
     *
     * ✅ CORRECTION : CLIENT ajouté — nécessaire pour BookingPage qui charge
     * le détail d'une chambre avant de créer une réservation.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(roomUseCase.getRoomById(id))
        );
    }

    // ──────────────────────────── CREATE ────────────────────────────

    /**
     * POST /api/rooms
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {

        RoomResponse created = roomUseCase.createRoom(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Chambre créée avec succès", created));
    }

    // ──────────────────────────── UPDATE ────────────────────────────

    /**
     * PUT /api/rooms/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request) {

        RoomResponse updated = roomUseCase.updateRoom(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Chambre mise à jour avec succès", updated)
        );
    }

    // ──────────────────────────── DELETE ────────────────────────────

    /**
     * DELETE /api/rooms/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomUseCase.deleteRoom(id);
        return ResponseEntity.ok(
                ApiResponse.success("Chambre supprimée avec succès")
        );
    }
}