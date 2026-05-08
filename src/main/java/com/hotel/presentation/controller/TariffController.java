package com.hotel.presentation.controller;

import com.hotel.application.dto.request.ApplyDiscountRequest;
import com.hotel.application.dto.request.CreateTariffRequest;
import com.hotel.application.dto.request.UpdateTariffRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.TariffResponse;
import com.hotel.application.usecase.TariffUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PRESENTATION LAYER — Tariff REST Controller.
 *
 *  GET    /api/tariffs                  → tous les tarifs
 *  POST   /api/tariffs                  → créer un tarif        (ADMIN)
 *  PUT    /api/tariffs/{id}             → modifier un tarif     (ADMIN)
 *  DELETE /api/tariffs/{id}             → supprimer un tarif    (ADMIN)
 *  PATCH  /api/tariffs/{id}/discount    → appliquer une remise  (ADMIN)
 */
@RestController
@RequestMapping("/tariffs")
@RequiredArgsConstructor
public class TariffController {

    private final TariffUseCase tariffUseCase;

    // ──────────────────────────── GET ALL ────────────────────────────

    /**
     * GET /api/tariffs
     * Accessible par ADMIN et RECEPTIONNISTE.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> getAllTariffs() {
        return ResponseEntity.ok(ApiResponse.success(tariffUseCase.getAllTariffs()));
    }

    // ──────────────────────────── GET BY ID ────────────────────────────

    /**
     * GET /api/tariffs/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<TariffResponse>> getTariffById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tariffUseCase.getTariffById(id)));
    }

    // ──────────────────────────── CREATE ────────────────────────────

    /**
     * POST /api/tariffs
     * Body : { name, season, room_type, price_per_night, discount_percent, start_date, end_date }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> createTariff(
            @Valid @RequestBody CreateTariffRequest request) {

        TariffResponse created = tariffUseCase.createTariff(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tarif créé avec succès", created));
    }

    // ──────────────────────────── UPDATE ────────────────────────────

    /**
     * PUT /api/tariffs/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> updateTariff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTariffRequest request) {

        TariffResponse updated = tariffUseCase.updateTariff(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tarif mis à jour", updated));
    }

    // ──────────────────────────── APPLY DISCOUNT ────────────────────────────

    /**
     * PATCH /api/tariffs/{id}/discount
     * Body : { discount }
     */
    @PatchMapping("/{id}/discount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> applyDiscount(
            @PathVariable Long id,
            @Valid @RequestBody ApplyDiscountRequest request) {

        TariffResponse updated = tariffUseCase.applyDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Remise appliquée", updated));
    }

    // ──────────────────────────── DELETE ────────────────────────────

    /**
     * DELETE /api/tariffs/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTariff(@PathVariable Long id) {
        tariffUseCase.deleteTariff(id);
        return ResponseEntity.ok(ApiResponse.success("Tarif supprimé avec succès"));
    }
}