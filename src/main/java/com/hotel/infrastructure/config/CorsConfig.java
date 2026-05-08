package com.hotel.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — CORS configuration.
 *
 * FIX : @Value injection d'une propriété comme List<String> ne fonctionne pas
 *       lorsque la valeur dans application.properties est une chaîne avec virgules
 *       (ex: cors.allowed-origins=http://localhost:4200,http://localhost:3000).
 *       Spring injecte la chaîne entière comme un seul élément → origins mal parsées.
 *
 *       SOLUTION : injecter comme String puis splitter manuellement sur la virgule.
 *       Cela fonctionne avec toutes les variantes de propriété (1 ou N origines).
 *
 * RAPPEL : Ce CorsConfigurationSource est automatiquement détecté par SecurityConfig
 *          via l'injection @RequiredArgsConstructor → plus besoin de le configurer
 *          manuellement dans SecurityConfig (mais SecurityConfig doit bien appeler
 *          http.cors(c -> c.configurationSource(corsConfigurationSource))).
 */
@Configuration
public class CorsConfig {

    // FIX : injecter en String (pas List<String>) pour éviter le problème de parsing CSV
    @Value("${cors.allowed-origins:http://localhost:4200,http://localhost:3000}")
    private String allowedOriginsRaw;

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // FIX : split manuel sur la virgule + trim de chaque valeur
        List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        config.setAllowedOrigins(origins);

        // Tous les verbes HTTP + OPTIONS (preflight)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers nécessaires pour JWT
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        // Cache le résultat du preflight pendant 1 heure
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}