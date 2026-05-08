package com.hotel.infrastructure.config;

import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * INFRASTRUCTURE LAYER — Seeds default users on startup (dev only).
 *
 * FIX : Ajout d'un compte client de test (client@hotel.com / Client@1234)
 *       pour pouvoir tester le login côté CLIENT sans avoir à créer
 *       manuellement un client via l'API.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner seedDefaultUsers() {
        return args -> {
            seedAdmin();
            seedReceptionniste();
            seedClientTest();   // ← AJOUT
            log.info("Data initialization complete — {} users in DB", userRepository.count());
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
        log.info("Admin account created → admin@hotel.com / Admin@1234");
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
        log.info("Receptionniste account created → reception@hotel.com / Recep@1234");
    }

    /**
     * Compte client de test.
     * Email  : client@hotel.com
     * Mot de passe : Client@1234
     *
     * Ce compte permet de tester le login client sans passer par la création
     * d'un client via l'interface admin/réception.
     */
    private void seedClientTest() {
        if (userRepository.existsByEmail("client@hotel.com")) return;

        User client = User.create(
                "Client Test",
                "client@hotel.com",
                passwordEncoder.encode("Client@1234"),
                Role.CLIENT
        );
        userRepository.save(client);
        log.info("Client test account created → client@hotel.com / Client@1234");
    }
}