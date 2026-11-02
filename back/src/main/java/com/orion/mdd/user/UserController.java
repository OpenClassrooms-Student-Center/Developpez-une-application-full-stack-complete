package com.orion.mdd.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Exposes the profile of the logged-in user.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the logged-in user. The front-end calls it when it starts, since it cannot read the
     * HttpOnly cookie that holds the token.
     *
     * @param jwt
     *            the verified token
     * @return the profile
     */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        String subject = Objects.requireNonNull(jwt.getSubject());
        return UserResponse.from(userService.getCurrentUser(Long.valueOf(subject)));
    }

}
