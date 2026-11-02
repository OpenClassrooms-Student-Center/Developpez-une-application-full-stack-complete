package com.orion.mdd.common.security;

import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.util.WebUtils;

import java.util.Map;

/**
 * Security rules of the API: a valid token in the {@value #TOKEN_COOKIE} cookie is required everywhere except
 * registration, and every write request needs a CSRF token.
 */
@Configuration
public class SecurityConfig {

    public static final String TOKEN_COOKIE = "mdd_token";

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
     * Declares the public routes, turns on the CSRF protection meant for single-page applications and reads the
     * token from the cookie.
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
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            Cookie cookie = WebUtils.getCookie(request, TOKEN_COOKIE);
                            return cookie == null ? null : cookie.getValue();
                        })
                        .jwt(Customizer.withDefaults()));
        return http.build();
    }

}
