package com.orion.mdd.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

/**
 * Security rules of the API: everything needs a logged-in user except registration, and every write request needs a
 * CSRF token.
 */
@Configuration
public class SecurityConfig {

    private static final String ARGON2 = "argon2";
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int PARALLELISM = 1;
    private static final int MEMORY_KIB = 19456;
    private static final int ITERATIONS = 2;

    /**
     * Hashes the passwords with Argon2id, at OWASP's minimum settings. The delegating encoder writes the algorithm
     * in front of each hash, so that it can change later without invalidating the accounts.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY_KIB,
                ITERATIONS);
        return new DelegatingPasswordEncoder(ARGON2, Map.of(ARGON2, argon2));
    }

    /**
     * Declares the public routes and turns on the CSRF protection meant for single-page applications.
     *
     * @param http
     *            the security configuration builder
     * @return the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.spa())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

}
