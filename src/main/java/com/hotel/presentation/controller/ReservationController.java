package com.hotel.presentation.controller;

import com.hotel.application.dto.request.CreateReservationRequest;
import com.hotel.application.dto.request.UpdateReservationRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.ReservationResponse;
import com.hotel.application.usecase.ReservationUseCase;
import com.hotel.domain.exception.ClientNotFoundException;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.infrastructure.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PRESENTATION LAYER — Reservation REST Controller.
 * Expose les endpoints attendus par le frontend React.
 *
 *  GET    /api/reservations                         → toutes les réservations (ADMIN, RECEPTIONNISTE)
 *  GET    /api/reservations/my                      → mes réservations (CLIENT)
 *  GET    /api/reservations/{id}                    → une réservation par id
 *  POST   /api/reservations                         → créer une réservation
 *  PUT    /api/reservations/{id}                    → modifier une réservation
 *  PATCH  /api/reservations/{id}/cancel             → annuler
 *  PATCH  /api/reservations/{id}/check-in           → check-in
 *  PATCH  /api/reservations/{id}/check-out          → check-out
 *  DELETE /api/reservations/{id}                    → supprimer (ADMIN only)
 */
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationUseCase reservationUseCase;
    private final ClientRepository    clientRepository;

    // ──────────────────────────── GET ALL ────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservations() {
        return ResponseEntity.ok(ApiResponse.success(reservationUseCase.getAllReservations()));
    }

    // ──────────────────────────── GET MY RESERVATIONS ────────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Resolve the client via the authenticated user's email
        String email    = userDetails.getUsername();
        var    client   = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email", email));
        Long   clientId = client.getId();

        return ResponseEntity.ok(ApiResponse.success(reservationUseCase.getMyReservations(clientId)));
    }

    // ──────────────────────────── GET BY ID ────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reservationUseCase.getReservationById(id)));
    }

    // ──────────────────────────── CREATE ────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {

        ReservationResponse created = reservationUseCase.createReservation(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Réservation créée avec succès", created));
    }

    // ──────────────────────────── UPDATE ────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequest request) {

        ReservationResponse updated = reservationUseCase.updateReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Réservation mise à jour", updated));
    }

    // ──────────────────────────── CANCEL ────────────────────────────

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable Long id) {
        ReservationResponse cancelled = reservationUseCase.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Réservation annulée", cancelled));
    }

    // ──────────────────────────── CHECK-IN ────────────────────────────

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkIn(@PathVariable Long id) {
        ReservationResponse updated = reservationUseCase.checkIn(id);
        return ResponseEntity.ok(ApiResponse.success("Check-in effectué", updated));
    }

    // ──────────────────────────── CHECK-OUT ────────────────────────────

    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkOut(@PathVariable Long id) {
        ReservationResponse updated = reservationUseCase.checkOut(id);
        return ResponseEntity.ok(ApiResponse.success("Check-out effectué", updated));
    }

    // ──────────────────────────── DELETE ────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationUseCase.deleteReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Réservation supprimée"));
    }
}