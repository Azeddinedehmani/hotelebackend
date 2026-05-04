package com.hotel.application.usecase;

import com.hotel.application.dto.request.LoginRequest;
import com.hotel.application.dto.request.RegisterRequest;
import com.hotel.application.dto.response.AuthResponse;
import com.hotel.application.dto.response.UserResponse;
import com.hotel.domain.exception.UnauthorizedException;
import com.hotel.domain.exception.UserNotFoundException;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.UserRepository;
import com.hotel.domain.service.UserDomainService;
import com.hotel.infrastructure.security.config.CustomUserDetails;
import com.hotel.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * APPLICATION LAYER — AuthUseCase Implementation.
 * Orchestrates domain model, domain service, and JWT infrastructure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthUseCaseImpl implements AuthUseCase {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.email());

        // 1. Domain invariant: email uniqueness
        userDomainService.ensureEmailIsUnique(request.email());

        // 2. Encode password
        String encodedPassword = userDomainService.encodePassword(request.password());

        // 3. Create domain user (domain validates invariants)
        User user = User.create(request.name(), request.email(), encodedPassword, request.role());

        // 4. Persist via repository port
        User savedUser = userRepository.save(user);

        // 5. Generate JWT
        var userDetails = new CustomUserDetails(savedUser);
        String token    = jwtService.generateToken(userDetails);

        log.info("User registered successfully: {}", savedUser.getEmail());
        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.from(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // 1. Find user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UnauthorizedException::invalidCredentials);

        // 2. Check account is active
        userDomainService.ensureUserIsActive(user);

        // 3. Verify password
        if (!userDomainService.passwordMatches(request.password(), user.getPassword())) {
            throw UnauthorizedException.invalidCredentials();
        }

        // 4. Generate JWT
        var userDetails = new CustomUserDetails(user);
        String token    = jwtService.generateToken(userDetails);

        log.info("Login successful for: {}", user.getEmail());
        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return UserResponse.from(user);
    }
}