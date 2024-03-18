package com.mddcore.usecases.ut.security;

import com.mddcore.domain.models.RefreshToken;
import com.mddcore.domain.models.User;
import com.mddcore.usecases.UseCaseExecutor;
import com.mddcore.usecases.user.token.*;
import com.mddinfrastructure.response.AuthResponse;
import com.mddinfrastructure.security.jwt.CookieJwt;
import com.mddinfrastructure.security.jwt.JwtService;
import com.mddinfrastructure.security.jwt.JwtTokenProvider;
import com.mddinfrastructure.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceUnitTest {
    @InjectMocks
    private JwtService jwtService;
    @Mock
    private CookieJwt cookieJwt;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UseCaseExecutor useCaseExecutor;

    @Test
    public void generateAuthResponse_ShouldReturnResponseEntityWithCookiesAndBody_ForValidUserDetails() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User user = mock(User.class);
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "jwtCookie").build();
        ResponseCookie jwtRefreshCookie = ResponseCookie.from("refresh", "RefreshToken").build();
        Long userId = 1L;
        String pictureUrl = "http://example.test/jpg";

        AuthResponse expectedBody = new AuthResponse(userId, pictureUrl);
        ResponseEntity<AuthResponse> expectedResponse = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(expectedBody);

        doReturn(user).when(userDetails).getUser();
        doReturn(userId).when(user).getId();
        doReturn(CompletableFuture.completedFuture(expectedResponse)).when(useCaseExecutor)
                .execute(any(), any(CreateRefreshTokenUseCase.InputValues.class), any());

        CompletableFuture<ResponseEntity<?>> resultFuture = jwtService.generateAuthResponse(userDetails, jwtCookie);
        ResponseEntity<?> result = resultFuture.join();

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getHeaders().get(HttpHeaders.SET_COOKIE)).contains(jwtCookie.toString());
        assertThat(result.getHeaders().get(HttpHeaders.SET_COOKIE)).contains(jwtRefreshCookie.toString());
        assertThat(result.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void generateLogoutResponse_ShouldReturnResponseEntityWithCookiesAndBody_ForLogoutSuccess() {
        Long authId = 1L;
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "").path("/").maxAge(0).build();
        ResponseCookie jwtRefreshCookie = ResponseCookie.from("refresh", "").path("/").maxAge(0).build();
        String expectedBody = "You've been signed out successfully!";

        ResponseEntity<?> expectedResponse = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(expectedBody);

        doReturn(authId).when(jwtTokenProvider).getAuthenticateUser();
        doReturn(CompletableFuture.completedFuture(expectedResponse))
                .when(useCaseExecutor)
                .execute(any(), any(DeleteRefreshTokenUseCase.InputValues.class), any());

        CompletableFuture<ResponseEntity<?>> resultFuture = jwtService.generateLogoutResponse();
        ResponseEntity<?> result = resultFuture.join();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().get(HttpHeaders.SET_COOKIE)).contains(jwtCookie.toString());
        assertThat(result.getHeaders().get(HttpHeaders.SET_COOKIE)).contains(jwtRefreshCookie.toString());
        assertThat(result.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void generateRefreshTokenResponse_ShouldReturnBadRequest_ForEmptyOrNullToken() {
        CompletableFuture<ResponseEntity<?>> responseForNullToken = jwtService.generateRefreshTokenResponse(null);
        CompletableFuture<ResponseEntity<?>> responseForEmptyToken = jwtService.generateRefreshTokenResponse("");

        CompletableFuture.allOf(responseForNullToken, responseForEmptyToken).join();

        assertThat(responseForNullToken.join().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseForEmptyToken.join().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseForNullToken.join().getBody()).isEqualTo("Refresh token is empty");
        assertThat(responseForEmptyToken.join().getBody()).isEqualTo("Refresh token is empty");
    }
}
