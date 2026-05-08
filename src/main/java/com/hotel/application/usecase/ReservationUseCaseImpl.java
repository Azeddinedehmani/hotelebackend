package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateReservationRequest;
import com.hotel.application.dto.request.UpdateReservationRequest;
import com.hotel.application.dto.response.ClientResponse;
import com.hotel.application.dto.response.ReservationResponse;
import com.hotel.application.dto.response.RoomResponse;
import com.hotel.domain.exception.ClientNotFoundException;
import com.hotel.domain.exception.ReservationNotFoundException;
import com.hotel.domain.exception.RoomNotAvailableException;
import com.hotel.domain.exception.RoomNotFoundException;
import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;
import com.hotel.domain.model.Room;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.domain.repository.ReservationRepository;
import com.hotel.domain.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * APPLICATION LAYER — ReservationUseCase Implementation
 *
 * CORRECTIF : checkOut() génère désormais automatiquement la facture
 * via InvoiceUseCase.generateInvoice() au moment du check-out.
 */
@Service
@Transactional
public class ReservationUseCaseImpl implements ReservationUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReservationUseCaseImpl.class);

    private final ReservationRepository reservationRepository;
    private final ClientRepository      clientRepository;
    private final RoomRepository        roomRepository;
    // ✅ AJOUT : injection de InvoiceUseCase pour générer la facture au check-out
    private final InvoiceUseCase        invoiceUseCase;

    public ReservationUseCaseImpl(ReservationRepository reservationRepository,
                                  ClientRepository clientRepository,
                                  RoomRepository roomRepository,
                                  InvoiceUseCase invoiceUseCase) {          // ✅ AJOUT
        this.reservationRepository = reservationRepository;
        this.clientRepository      = clientRepository;
        this.roomRepository        = roomRepository;
        this.invoiceUseCase        = invoiceUseCase;                        // ✅ AJOUT
    }

    // ────────────────── Queries ──────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long clientId) {
        return reservationRepository.findByClientId(clientId).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        return enrich(findOrThrow(id));
    }

    // ────────────────── Commands ──────────────────

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ClientNotFoundException(request.clientId()));

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RoomNotFoundException(request.roomId()));

        if (!room.isAvailable()) {
            throw new RoomNotAvailableException(
                    request.roomId(),
                    request.checkInDate().toString(),
                    request.checkOutDate().toString());
        }

        if (reservationRepository.hasOverlappingReservation(
                request.roomId(), request.checkInDate(), request.checkOutDate(), null)) {
            throw new RoomNotAvailableException(
                    request.roomId(),
                    request.checkInDate().toString(),
                    request.checkOutDate().toString());
        }

        Reservation reservation = Reservation.create(
                request.clientId(), request.roomId(),
                request.checkInDate(), request.checkOutDate(),
                request.guests(), request.notes());

        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation créée — id={}, roomId={}", saved.getId(), saved.getRoomId());
        return enrich(saved);
    }

    @Override
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request) {
        Reservation reservation = findOrThrow(id);

        if (reservationRepository.hasOverlappingReservation(
                reservation.getRoomId(), request.checkInDate(), request.checkOutDate(), id)) {
            throw new RoomNotAvailableException(
                    reservation.getRoomId(),
                    request.checkInDate().toString(),
                    request.checkOutDate().toString());
        }

        reservation.updateDetails(request.checkInDate(), request.checkOutDate(),
                request.guests(), request.notes());

        if (request.status() != null) {
            applyStatusTransition(reservation, request.status());
        }

        Reservation updated = reservationRepository.save(reservation);
        log.info("Réservation mise à jour — id={}", updated.getId());
        return enrich(updated);
    }

    @Override
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = findOrThrow(id);
        reservation.cancel();
        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation annulée — id={}", id);
        return enrich(saved);
    }

    @Override
    public ReservationResponse checkIn(Long id) {
        Reservation reservation = findOrThrow(id);

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(reservation.getRoomId()));

        if (!room.isAvailable()) {
            throw new RoomNotAvailableException(
                    room.getId(),
                    reservation.getCheckInDate().toString(),
                    reservation.getCheckOutDate().toString());
        }

        reservation.checkIn();
        room.markAsOccupied();
        roomRepository.save(room);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Check-in effectué — reservationId={}, roomId={}, at={}",
                saved.getId(), room.getId(), saved.getActualCheckInAt());
        return enrich(saved);
    }

    /**
     * CHECK-OUT avec génération automatique de la facture.
     *
     * Étapes :
     *  1. Valide que la réservation est CHECKED_IN (domain)
     *  2. Remet la chambre en AVAILABLE
     *  3. Enregistre l'heure réelle du check-out
     *  4. ✅ Génère la facture (idempotente : ne crée pas de doublon)
     */
    @Override
    public ReservationResponse checkOut(Long id) {
        Reservation reservation = findOrThrow(id);

        // 1. Transition de statut + enregistrement de l'heure réelle
        reservation.checkOut();

        // 2. Chambre → AVAILABLE
        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(reservation.getRoomId()));
        room.markAsAvailable();
        roomRepository.save(room);

        Reservation saved = reservationRepository.save(reservation);

        // ✅ 3. Génération automatique de la facture
        long       nights       = saved.getDurationNights();
        BigDecimal pricePerNight = room.getPrice();
        BigDecimal discountRate  = BigDecimal.ZERO;   // pas de remise par défaut (extensible)

        invoiceUseCase.generateInvoice(saved.getId(), nights, pricePerNight, discountRate);

        log.info("Check-out effectué + facture générée — reservationId={}, roomId={}, nights={}, total={}",
                saved.getId(), room.getId(), nights,
                pricePerNight.multiply(java.math.BigDecimal.valueOf(nights)));

        return enrich(saved);
    }

    @Override
    public void deleteReservation(Long id) {
        findOrThrow(id);
        reservationRepository.deleteById(id);
        log.info("Réservation supprimée — id={}", id);
    }

    // ────────────────── Helpers ──────────────────

    private Reservation findOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private ReservationResponse enrich(Reservation r) {
        ClientResponse client = clientRepository.findById(r.getClientId())
                .map(ClientResponse::from).orElse(null);
        RoomResponse room = roomRepository.findById(r.getRoomId())
                .map(RoomResponse::from).orElse(null);
        return ReservationResponse.from(r, client, room);
    }

    private void applyStatusTransition(Reservation reservation, ReservationStatus target) {
        switch (target) {
            case CONFIRMED   -> reservation.confirm();
            case CANCELLED   -> reservation.cancel();
            case CHECKED_IN  -> reservation.checkIn();
            case CHECKED_OUT -> reservation.checkOut();
            default -> { /* PENDING — état initial */ }
        }
    }
}