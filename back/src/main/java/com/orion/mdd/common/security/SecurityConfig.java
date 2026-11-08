package com.orion.mdd.common.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.WebUtils;

import java.util.Map;

/**
 * Security rules of the API: a valid token in the {@value #TOKEN_COOKIE} cookie is required everywhere except
 * the login routes, the Swagger documentation and the error page, and every write request needs a CSRF token.
 */
@Configuration
public class SecurityConfig {

    public static final String TOKEN_COOKIE = "mdd_token";
    private static final RequestMatcher AUTH_ROUTES = PathPatternRequestMatcher.withDefaults().matcher("/api/auth/**");

    private static final String ARGON2 = "argon2";
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int PARALLELISM = 1;
    private static final int MEMORY_KIB = 19456;
    private static final int ITERATIONS = 2;

    private final SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(SecurityErrorHandler securityErrorHandler) {
        this.securityErrorHandler = securityErrorHandler;
    }

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
     * Declares the public routes, reads the token from the cookie and returns the errors as Problem Details.
     *
     * @param http
     *            the security configuration builder
     * @return the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf
                        .spa()
                        .withObjectPostProcessor(protectRequestsWithAToken()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_ROUTES).permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(this::tokenFromCookie)
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler));
        return http.build();
    }

    /**
     * Reads the token from its cookie, except on the routes that log in and out: they issue or clear the token, so
     * they must work even when the browser still holds one that is no longer valid, signed with another secret for
     * instance.
     */
    private @Nullable String tokenFromCookie(HttpServletRequest request) {
        if (AUTH_ROUTES.matches(request)) {
            return null;
        }
        Cookie cookie = WebUtils.getCookie(request, TOKEN_COOKIE);
        return cookie == null ? null : cookie.getValue();
    }

    /**
     * Spring Security skips the CSRF check on requests that carry a token, because a token normally travels in a
     * header that the browser never adds by itself. Here it travels in a cookie, which the browser does add, so the
     * check must stay.
     */
    private ObjectPostProcessor<CsrfFilter> protectRequestsWithAToken() {
        return new ObjectPostProcessor<>() {

            @Override
            public <O extends CsrfFilter> O postProcess(O filter) {
                filter.setRequireCsrfProtectionMatcher(CsrfFilter.DEFAULT_CSRF_MATCHER);
                return filter;
            }

        };
    }

}
