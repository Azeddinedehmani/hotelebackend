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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * APPLICATION LAYER — AuthUseCase Implementation.
 */
@Service
@Transactional
public class AuthUseCaseImpl implements AuthUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthUseCaseImpl.class);

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthUseCaseImpl(UserRepository userRepository,
                           UserDomainService userDomainService,
                           JwtService jwtService,
                           UserDetailsService userDetailsService) {
        this.userRepository    = userRepository;
        this.userDomainService = userDomainService;
        this.jwtService        = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.email());

        userDomainService.ensureEmailIsUnique(request.email());

        String encodedPassword = userDomainService.encodePassword(request.password());
        User user = User.create(request.name(), request.email(), encodedPassword, request.role());
        User savedUser = userRepository.save(user);

        var userDetails = new CustomUserDetails(savedUser);
        String token    = jwtService.generateToken(userDetails);

        log.info("User registered successfully: {}", savedUser.getEmail());
        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.from(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UnauthorizedException::invalidCredentials);

        userDomainService.ensureUserIsActive(user);

        if (!userDomainService.passwordMatches(request.password(), user.getPassword())) {
            throw UnauthorizedException.invalidCredentials();
        }

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