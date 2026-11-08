package com.orion.mdd.auth;

import com.orion.mdd.common.security.JwtProperties;
import com.orion.mdd.common.security.SecurityConfig;
import com.orion.mdd.user.User;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Builds the cookie that keeps a user logged in.
 */
@Service
public class AuthCookieService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public AuthCookieService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Signs a token for the user and wraps it in a cookie that scripts cannot read.
     *
     * @param user
     *            the user, already saved in the database
     * @return the cookie, which lasts as long as the token
     */
    public ResponseCookie create(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return tokenCookie(token, jwtProperties.expiration());
    }

    /**
     * Builds an empty cookie that the browser deletes at once. The token itself stays valid until it expires: the
     * API keeps no list of the tokens it issued.
     *
     * @return the cookie that replaces the token cookie
     */
    public ResponseCookie clear() {
        return tokenCookie("", Duration.ZERO);
    }

    /**
     * A browser deletes a cookie only if the new one has the same name, path and attributes, so both cookies are
     * built here.
     */
    private static ResponseCookie tokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from(SecurityConfig.TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

}
