package com.hotel.domain.repository;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff;
import com.hotel.domain.model.Tariff.Season;

import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — Tariff Repository Port.
 * Clean interface, no framework dependency.
 */
public interface TariffRepository {

    Tariff save(Tariff tariff);

    Optional<Tariff> findById(Long id);

    List<Tariff> findAll();

    List<Tariff> findBySeason(Season season);

    List<Tariff> findByRoomType(RoomType roomType);

    List<Tariff> findByActive(boolean active);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();
}