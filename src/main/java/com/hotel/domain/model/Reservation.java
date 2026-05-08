package com.hotel.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * DOMAIN LAYER — Reservation Aggregate Root  (v2 — check-in/out timestamps)
 */
public class Reservation {

    private Long              id;
    private Long              clientId;
    private Long              roomId;
    private LocalDate         checkInDate;
    private LocalDate         checkOutDate;
    private ReservationStatus status;
    private int               guests;
    private String            notes;
    private LocalDateTime     actualCheckInAt;
    private LocalDateTime     actualCheckOutAt;

    protected Reservation() {}

    private Reservation(Builder b) {
        this.id               = b.id;
        this.clientId         = b.clientId;
        this.roomId           = b.roomId;
        this.checkInDate      = b.checkInDate;
        this.checkOutDate     = b.checkOutDate;
        this.status           = b.status;
        this.guests           = b.guests;
        this.notes            = b.notes;
        this.actualCheckInAt  = b.actualCheckInAt;
        this.actualCheckOutAt = b.actualCheckOutAt;
    }

    public static Reservation create(Long clientId, Long roomId,
                                     LocalDate checkInDate, LocalDate checkOutDate,
                                     int guests, String notes) {
        validateClientId(clientId);
        validateRoomId(roomId);
        validateDates(checkInDate, checkOutDate);
        validateGuests(guests);
        return new Builder().clientId(clientId).roomId(roomId)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(ReservationStatus.PENDING).guests(guests).notes(notes).build();
    }

    public static Reservation reconstitute(Long id, Long clientId, Long roomId,
                                           LocalDate checkInDate, LocalDate checkOutDate,
                                           ReservationStatus status, int guests, String notes,
                                           LocalDateTime actualCheckInAt,
                                           LocalDateTime actualCheckOutAt) {
        return new Builder().id(id).clientId(clientId).roomId(roomId)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(status).guests(guests).notes(notes)
                .actualCheckInAt(actualCheckInAt).actualCheckOutAt(actualCheckOutAt).build();
    }

    public void confirm() {
        if (this.status == ReservationStatus.CANCELLED)
            throw new IllegalStateException("Impossible de confirmer une réservation annulée");
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == ReservationStatus.CHECKED_IN || this.status == ReservationStatus.CHECKED_OUT)
            throw new IllegalStateException("Impossible d'annuler une réservation en cours ou terminée");
        this.status = ReservationStatus.CANCELLED;
    }

    /** Check-in : réservation doit être CONFIRMED — enregistre l'heure réelle. */
    public void checkIn() {
        if (this.status != ReservationStatus.CONFIRMED)
            throw new IllegalStateException(
                "La réservation doit être CONFIRMED avant le check-in (statut actuel : " + this.status + ")");
        this.status          = ReservationStatus.CHECKED_IN;
        this.actualCheckInAt = LocalDateTime.now();
    }

    /** Check-out : réservation doit être CHECKED_IN — enregistre l'heure réelle. */
    public void checkOut() {
        if (this.status != ReservationStatus.CHECKED_IN)
            throw new IllegalStateException(
                "Le client doit être en CHECKED_IN avant le check-out (statut actuel : " + this.status + ")");
        this.status           = ReservationStatus.CHECKED_OUT;
        this.actualCheckOutAt = LocalDateTime.now();
    }

    public void updateDetails(LocalDate checkInDate, LocalDate checkOutDate, int guests, String notes) {
        if (this.status == ReservationStatus.CANCELLED || this.status == ReservationStatus.CHECKED_OUT)
            throw new IllegalStateException("Impossible de modifier une réservation annulée ou terminée");
        validateDates(checkInDate, checkOutDate);
        validateGuests(guests);
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guests       = guests;
        this.notes        = notes;
    }

    public long getDurationNights() { return ChronoUnit.DAYS.between(checkInDate, checkOutDate); }
    public boolean isActive() {
        return status == ReservationStatus.PENDING
            || status == ReservationStatus.CONFIRMED
            || status == ReservationStatus.CHECKED_IN;
    }

    private static void validateClientId(Long v) { if (v == null) throw new IllegalArgumentException("Client obligatoire"); }
    private static void validateRoomId(Long v)   { if (v == null) throw new IllegalArgumentException("Chambre obligatoire"); }
    private static void validateDates(LocalDate ci, LocalDate co) {
        if (ci == null) throw new IllegalArgumentException("Date d'arrivée obligatoire");
        if (co == null) throw new IllegalArgumentException("Date de départ obligatoire");
        if (!co.isAfter(ci)) throw new IllegalArgumentException("Le départ doit être après l'arrivée");
    }
    private static void validateGuests(int g) {
        if (g < 1) throw new IllegalArgumentException("Minimum 1 personne");
        if (g > 20) throw new IllegalArgumentException("Maximum 20 personnes");
    }

    public Long              getId()               { return id; }
    public Long              getClientId()         { return clientId; }
    public Long              getRoomId()           { return roomId; }
    public LocalDate         getCheckInDate()      { return checkInDate; }
    public LocalDate         getCheckOutDate()     { return checkOutDate; }
    public ReservationStatus getStatus()           { return status; }
    public int               getGuests()           { return guests; }
    public String            getNotes()            { return notes; }
    public LocalDateTime     getActualCheckInAt()  { return actualCheckInAt; }
    public LocalDateTime     getActualCheckOutAt() { return actualCheckOutAt; }

    public static final class Builder {
        private Long id; private Long clientId; private Long roomId;
        private LocalDate checkInDate; private LocalDate checkOutDate;
        private ReservationStatus status; private int guests; private String notes;
        private LocalDateTime actualCheckInAt; private LocalDateTime actualCheckOutAt;

        public Builder id(Long v)                       { this.id = v; return this; }
        public Builder clientId(Long v)                 { this.clientId = v; return this; }
        public Builder roomId(Long v)                   { this.roomId = v; return this; }
        public Builder checkInDate(LocalDate v)         { this.checkInDate = v; return this; }
        public Builder checkOutDate(LocalDate v)        { this.checkOutDate = v; return this; }
        public Builder status(ReservationStatus v)      { this.status = v; return this; }
        public Builder guests(int v)                    { this.guests = v; return this; }
        public Builder notes(String v)                  { this.notes = v; return this; }
        public Builder actualCheckInAt(LocalDateTime v) { this.actualCheckInAt = v; return this; }
        public Builder actualCheckOutAt(LocalDateTime v){ this.actualCheckOutAt = v; return this; }
        public Reservation build()                      { return new Reservation(this); }
    }

    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Reservation r)) return false; return Objects.equals(id, r.id); }
    @Override public int hashCode()           { return Objects.hash(id); }
    @Override public String toString()        { return "Reservation{id=" + id + ", status=" + status + "}"; }
}