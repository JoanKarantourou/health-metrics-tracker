package com.healthmetrics.tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Health Metrics Tracker application.
 *
 * TEMPORARY CONFIGURATION:
 * This configuration DISABLES all security to allow free API access during development.
 * In a production environment, you would implement proper authentication and authorization.
 *
 * This is similar to disabling [Authorize] attributes in ASP.NET Core during development.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain to permit all requests without authentication.
     *
     * @param http HttpSecurity object to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /// Disable CSRF protection (Cross-Site Request Forgery)
                /// This is safe for APIs that don't use session-based authentication
                .csrf(csrf -> csrf.disable())

                /// Allow all requests without authentication
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}