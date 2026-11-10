package com.orion.mdd.user;

import com.orion.mdd.common.security.AuthCookieService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final AuthCookieService authCookieService;

    public UserController(UserService userService, AuthCookieService authCookieService) {
        this.userService = userService;
        this.authCookieService = authCookieService;
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
        return UserResponse.from(userService.getCurrentUser(userId(jwt)));
    }

    /**
     * Changes the profile of the logged-in user. The token stays valid, since it names the account by its id.
     *
     * @param jwt
     *            the verified token
     * @param request
     *            the new username and e-mail address
     * @return the updated profile
     */
    @PutMapping("/me")
    @ApiResponse(responseCode = "200", description = "Profile updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid data, one message per field in errors",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Not logged in, or the account no longer exists",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Username or e-mail already taken by another user",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileRequest request) {
        return UserResponse.from(userService.updateProfile(userId(jwt), request));
    }

    /**
     * Changes the password of the logged-in user, who must give the current one.
     *
     * @param jwt
     *            the verified token
     * @param request
     *            the current password and the new one
     * @return an empty response
     */
    @PutMapping("/me/password")
    @ApiResponse(responseCode = "204", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "Invalid data or wrong current password, one message per field",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Not logged in, or the account no longer exists",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> changeMyPassword(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userId(jwt), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the account of the logged-in user, with its data, and logs them out.
     *
     * @param jwt
     *            the verified token
     * @return an empty response with the cookie that deletes the token cookie
     */
    @DeleteMapping("/me")
    @ApiResponse(responseCode = "204", description = "Account deleted, cookie deleted")
    @ApiResponse(responseCode = "401", description = "Not logged in",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteAccount(userId(jwt));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.clear().toString())
                .build();
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }

}
