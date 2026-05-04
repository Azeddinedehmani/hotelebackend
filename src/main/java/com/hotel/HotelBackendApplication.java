package com.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Hotel Management Backend — Clean Architecture
 * Layers: domain → application → infrastructure → presentation
 */
@SpringBootApplication
@EnableConfigurationProperties
public class HotelBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelBackendApplication.class, args);
    }
}