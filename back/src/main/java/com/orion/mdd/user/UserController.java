package com.orion.mdd.user;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
    @ApiResponse(responseCode = "200", description = "The logged-in user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "401", description = "Not logged in, or the account no longer exists",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        String subject = Objects.requireNonNull(jwt.getSubject());
        return UserResponse.from(userService.getCurrentUser(Long.valueOf(subject)));
    }

}
