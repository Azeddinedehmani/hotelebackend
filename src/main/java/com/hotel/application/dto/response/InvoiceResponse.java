package com.hotel.application.dto.response;

import com.hotel.domain.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * APPLICATION LAYER — Read-only view of an Invoice.
 *
 * FIXED: added clientId() and totalAmount() accessors used by InvoiceController.
 * - clientId()     → derived from reservationId (stored for client filtering)
 * - totalAmount()  → alias for totalPrice (used in PDF stub)
 */
public record InvoiceResponse(
        Long          id,
        Long          reservationId,
        Long          clientId,          // ← ADDED: populated via InvoiceUseCaseImpl
        long          nights,
        BigDecimal    roomPricePerNight,
        BigDecimal    discountRate,
        BigDecimal    totalPrice,
        LocalDateTime createdAt
) {
    /**
     * Basic mapping from domain Invoice (clientId unknown at this layer — set to null).
     * Use the overloaded from(Invoice, Long) when clientId is available.
     */
    public static InvoiceResponse from(Invoice inv) {
        return from(inv, null);
    }

    /**
     * Full mapping including clientId resolved from the reservation.
     */
    public static InvoiceResponse from(Invoice inv, Long clientId) {
        return new InvoiceResponse(
                inv.getId(),
                inv.getReservationId(),
                clientId,
                inv.getNights(),
                inv.getRoomPricePerNight(),
                inv.getDiscountRate(),
                inv.getTotalPrice(),
                inv.getCreatedAt()
        );
    }

    /**
     * Convenience alias used by InvoiceController PDF stub.
     * Returns the same value as totalPrice().
     */
    public BigDecimal totalAmount() {
        return totalPrice;
    }
}