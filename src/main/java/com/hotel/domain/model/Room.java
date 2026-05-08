package com.hotel.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DOMAIN LAYER — Room Aggregate Root
 * Pure Java object — no JPA, no Spring, no framework.
 */
public class Room {

    private Long       id;
    private String     number;
    private RoomType   type;
    private BigDecimal price;
    private int        capacity;
    private RoomStatus status;
    private String     description;

    // ────────────────── Constructors ──────────────────

    protected Room() {}

    private Room(Builder builder) {
        this.id          = builder.id;
        this.number      = builder.number;
        this.type        = builder.type;
        this.price       = builder.price;
        this.capacity    = builder.capacity;
        this.status      = builder.status;
        this.description = builder.description;
    }

    // ────────────────── Factory Methods ──────────────────

    /**
     * Creates a new Room (creation flow).
     * Enforces domain invariants.
     */
    public static Room create(String number, RoomType type, BigDecimal price,
                              int capacity, String description) {
        validateNumber(number);
        validateType(type);
        validatePrice(price);
        validateCapacity(capacity);

        return new Builder()
                .number(number.trim())
                .type(type)
                .price(price)
                .capacity(capacity)
                .status(RoomStatus.AVAILABLE)   // default status on creation
                .description(description != null ? description.trim() : null)
                .build();
    }

    /**
     * Reconstitute a Room from persistence (no validation — data already validated).
     */
    public static Room reconstitute(Long id, String number, RoomType type,
                                    BigDecimal price, int capacity,
                                    RoomStatus status, String description) {
        return new Builder()
                .id(id)
                .number(number)
                .type(type)
                .price(price)
                .capacity(capacity)
                .status(status)
                .description(description)
                .build();
    }

    // ────────────────── Domain Behaviours ──────────────────

    public void updateDetails(String number, RoomType type, BigDecimal price,
                              int capacity, String description) {
        validateNumber(number);
        validateType(type);
        validatePrice(price);
        validateCapacity(capacity);

        this.number      = number.trim();
        this.type        = type;
        this.price       = price;
        this.capacity    = capacity;
        this.description = description != null ? description.trim() : null;
    }

    public void markAsOccupied() {
        if (this.status == RoomStatus.OCCUPIED) {
            throw new IllegalStateException("La chambre est déjà occupée");
        }
        this.status = RoomStatus.OCCUPIED;
    }

    public void markAsAvailable() {
        this.status = RoomStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == RoomStatus.AVAILABLE;
    }

    // ────────────────── Domain Invariants ──────────────────

    private static void validateNumber(String number) {
        if (number == null || number.isBlank())
            throw new IllegalArgumentException("Le numéro de chambre ne peut pas être vide");
        if (number.trim().length() > 10)
            throw new IllegalArgumentException("Le numéro de chambre ne peut pas dépasser 10 caractères");
    }

    private static void validateType(RoomType type) {
        if (type == null)
            throw new IllegalArgumentException("Le type de chambre est obligatoire");
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null)
            throw new IllegalArgumentException("Le prix est obligatoire");
        if (price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Le prix doit être supérieur à 0");
    }

    private static void validateCapacity(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("La capacité doit être supérieure à 0");
        if (capacity > 20)
            throw new IllegalArgumentException("La capacité ne peut pas dépasser 20 personnes");
    }

    // ────────────────── Getters ──────────────────

    public Long       getId()          { return id; }
    public String     getNumber()      { return number; }
    public RoomType   getType()        { return type; }
    public BigDecimal getPrice()       { return price; }
    public int        getCapacity()    { return capacity; }
    public RoomStatus getStatus()      { return status; }
    public String     getDescription() { return description; }

    // ────────────────── Builder ──────────────────

    public static final class Builder {
        private Long       id;
        private String     number;
        private RoomType   type;
        private BigDecimal price;
        private int        capacity;
        private RoomStatus status;
        private String     description;

        public Builder id(Long id)                  { this.id = id; return this; }
        public Builder number(String v)              { this.number = v; return this; }
        public Builder type(RoomType v)              { this.type = v; return this; }
        public Builder price(BigDecimal v)           { this.price = v; return this; }
        public Builder capacity(int v)               { this.capacity = v; return this; }
        public Builder status(RoomStatus v)          { this.status = v; return this; }
        public Builder description(String v)         { this.description = v; return this; }

        public Room build() { return new Room(this); }
    }

    // ────────────────── equals / hashCode ──────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room r)) return false;
        return Objects.equals(number, r.number);
    }

    @Override
    public int hashCode() { return Objects.hash(number); }

    @Override
    public String toString() {
        return "Room{id=" + id + ", number='" + number + "', type=" + type + ", status=" + status + "}";
    }
}