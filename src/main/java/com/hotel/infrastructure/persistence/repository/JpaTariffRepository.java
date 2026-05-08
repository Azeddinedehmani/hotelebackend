package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff.Season;
import com.hotel.infrastructure.persistence.entity.TariffEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository for TariffEntity.
 * Only used by TariffRepositoryAdapter.
 */
public interface JpaTariffRepository extends JpaRepository<TariffEntity, Long> {

    List<TariffEntity> findBySeason(Season season);

    List<TariffEntity> findByRoomType(RoomType roomType);

    List<TariffEntity> findByActive(boolean active);
}