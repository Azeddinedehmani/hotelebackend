package com.hotel.infrastructure.config;

import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * INFRASTRUCTURE LAYER — Seeds default users on startup (dev only).
 * Remove or guard with @Profile("dev") for production.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDefaultUsers() {
        return args -> {
            seedAdmin();
            seedReceptionniste();
            log.info("✅ Data initialization complete — {} users in DB", userRepository.count());
        };
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@hotel.com")) return;

        User admin = User.create(
                "Administrateur",
                "admin@hotel.com",
                passwordEncoder.encode("Admin@1234"),
                Role.ADMIN
        );
        userRepository.save(admin);
        log.info("🔑 Admin account created → admin@hotel.com / Admin@1234");
    }

    private void seedReceptionniste() {
        if (userRepository.existsByEmail("reception@hotel.com")) return;

        User reception = User.create(
                "Réceptionniste Démo",
                "reception@hotel.com",
                passwordEncoder.encode("Recep@1234"),
                Role.RECEPTIONNISTE
        );
        userRepository.save(reception);
        log.info("🔑 Receptionniste account created → reception@hotel.com / Recep@1234");
    }
}