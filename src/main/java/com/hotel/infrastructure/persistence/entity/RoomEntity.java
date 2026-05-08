package com.hotel.infrastructure.persistence.entity;

import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for Room.
 * Completely isolated from the domain model.
 */
@Entity
@Table(
    name = "rooms",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rooms_number", columnNames = "number")
    }
)
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(length = 500)
    private String description;

    // ────────────────── Constructors ──────────────────

    public RoomEntity() {}

    private RoomEntity(Builder builder) {
        this.id          = builder.id;
        this.number      = builder.number;
        this.type        = builder.type;
        this.price       = builder.price;
        this.capacity    = builder.capacity;
        this.status      = builder.status;
        this.description = builder.description;
    }

    // ────────────────── Builder ──────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long       id;
        private String     number;
        private RoomType   type;
        private BigDecimal price;
        private int        capacity;
        private RoomStatus status;
        private String     description;

        public Builder id(Long id)              { this.id = id; return this; }
        public Builder number(String v)          { this.number = v; return this; }
        public Builder type(RoomType v)          { this.type = v; return this; }
        public Builder price(BigDecimal v)       { this.price = v; return this; }
        public Builder capacity(int v)           { this.capacity = v; return this; }
        public Builder status(RoomStatus v)      { this.status = v; return this; }
        public Builder description(String v)     { this.description = v; return this; }

        public RoomEntity build() { return new RoomEntity(this); }
    }

    // ────────────────── Getters / Setters ──────────────────

    public Long       getId()                        { return id; }
    public void       setId(Long id)                 { this.id = id; }

    public String     getNumber()                    { return number; }
    public void       setNumber(String number)       { this.number = number; }

    public RoomType   getType()                      { return type; }
    public void       setType(RoomType type)         { this.type = type; }

    public BigDecimal getPrice()                     { return price; }
    public void       setPrice(BigDecimal price)     { this.price = price; }

    public int        getCapacity()                  { return capacity; }
    public void       setCapacity(int capacity)      { this.capacity = capacity; }

    public RoomStatus getStatus()                    { return status; }
    public void       setStatus(RoomStatus status)   { this.status = status; }

    public String     getDescription()               { return description; }
    public void       setDescription(String desc)    { this.description = desc; }
}