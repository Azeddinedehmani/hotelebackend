package com.hotel.application.usecase;

import com.hotel.application.dto.request.ApplyDiscountRequest;
import com.hotel.application.dto.request.CreateTariffRequest;
import com.hotel.application.dto.request.UpdateTariffRequest;
import com.hotel.application.dto.response.TariffResponse;

import java.util.List;

/**
 * APPLICATION LAYER — Tariff Use Case Port.
 * Tous les endpoints appelés par le frontend React :
 *   GET    /tariffs
 *   POST   /tariffs
 *   PUT    /tariffs/{id}
 *   DELETE /tariffs/{id}
 *   PATCH  /tariffs/{id}/discount
 */
public interface TariffUseCase {

    List<TariffResponse> getAllTariffs();

    TariffResponse getTariffById(Long id);

    TariffResponse createTariff(CreateTariffRequest request);

    TariffResponse updateTariff(Long id, UpdateTariffRequest request);

    TariffResponse applyDiscount(Long id, ApplyDiscountRequest request);

    void deleteTariff(Long id);
}