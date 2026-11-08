package com.orion.mdd.common.security;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Settings of the tokens, read from the {@code mdd.jwt.*} properties.
 *
 * @param secret
 *            the key that signs and verifies the tokens, at least 32 characters long
 * @param expiration
 *            how long a token stays valid
 */
@ConfigurationProperties(prefix = "mdd.jwt")
@Validated
public record JwtProperties(
        @NotNull @Size(min = 32) String secret,
        @NotNull Duration expiration) {

}
