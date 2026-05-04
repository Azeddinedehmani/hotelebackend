package com.hotel.infrastructure.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — JWT configuration properties.
 * Bound from application.yml → jwt.*
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /** Secret key for signing JWT tokens (must be 256-bit minimum). */
    private String secret;

    /** Access token expiration in milliseconds (default: 24h). */
    private long expiration = 86400000L;

    /** Refresh token expiration in milliseconds (default: 7 days). */
    private long refreshExpiration = 604800000L;
}