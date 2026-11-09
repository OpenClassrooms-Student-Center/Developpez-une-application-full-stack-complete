package com.orion.mdd.auth;

import com.orion.mdd.Csrf;
import com.orion.mdd.IntegrationTest;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class LoginAndLogoutIntegrationTests {

    private static final String EMAIL = "arthur.dent@example.com";
    private static final String PASSWORD = "Dont-Panic-42";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthCookieService authCookieService;

    private User user;

    @BeforeEach
    void createAnAccount() {
        user = userRepository.save(new User("arthur.dent", EMAIL, passwordEncoder.encode(PASSWORD)));
    }

    @Test
    void logsTheUserInWithTheirEmailAndPassword() throws Exception {
        // Act
        MvcResult result = logIn(EMAIL, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("arthur.dent"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        // Assert
        String cookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(cookie).startsWith("mdd_token=").contains("Max-Age=86400", "HttpOnly");
        mockMvc.perform(get("/api/users/me").cookie(tokenCookie(result)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("arthur.dent"));
    }

    @ParameterizedTest
    @CsvSource({
            "arthur.dent@example.com, Wrong-Password-42",
            "ford.prefect@example.com, Dont-Panic-42"
    })
    void refusesWrongCredentialsWithoutSayingWhichOneIsWrong(String email, String password) throws Exception {
        // Act
        ResultActions response = logIn(email, password);

        // Assert
        response.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("E-mail ou mot de passe incorrect."))
                .andExpect(jsonPath("$.errors").doesNotExist());
        assertThat(response.andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "'', Dont-Panic-42, email",
            "arthur.dent, Dont-Panic-42, email",
            "arthur.dent@example.com, '', password"
    })
    void refusesAnInvalidField(String email, String password, String invalidField) throws Exception {
        // Act
        ResultActions response = logIn(email, password);

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + invalidField).isString());
    }

    @Test
    void logsInDespiteATokenCookieThatIsNoLongerValid() throws Exception {
        // Arrange: a token signed with another secret, after a change of secret for instance
        Cookie invalidToken = invalidTokenCookie();

        // Act
        ResultActions response = logIn(EMAIL, PASSWORD, invalidToken);

        // Assert
        response.andExpect(status().isOk());
    }

    @Test
    void logsTheUserOutByClearingTheTokenCookie() throws Exception {
        // Arrange
        Cookie token = tokenCookie(logIn(EMAIL, PASSWORD).andReturn());

        // Act
        ResultActions response = logOut(token);

        // Assert
        response.andExpect(status().isNoContent());
        assertThat(response.andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE))
                .startsWith("mdd_token=;")
                .contains("Max-Age=0", "Path=/", "Secure", "HttpOnly", "SameSite=Strict");
    }

    @Test
    void logsOutEvenWithATokenCookieThatIsNoLongerValid() throws Exception {
        // Act
        ResultActions response = logOut(invalidTokenCookie());

        // Assert
        response.andExpect(status().isNoContent());
    }

    @Test
    void refusesALogoutWithoutACsrfToken() throws Exception {
        // Arrange: another site cannot log the user out
        Cookie token = tokenCookie(logIn(EMAIL, PASSWORD).andReturn());

        // Act
        ResultActions response = mockMvc.perform(post("/api/auth/logout").cookie(token));

        // Assert
        response.andExpect(status().isForbidden());
    }

    /** Sends what a browser sends: a first call gives the CSRF cookie, then the POST repeats it in a header. */
    private ResultActions logIn(String email, String password, Cookie... otherCookies) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/auth/login")
                .with(Csrf.from(mockMvc))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password));
        if (otherCookies.length > 0) {
            request.cookie(otherCookies);
        }
        return mockMvc.perform(request);
    }

    private ResultActions logOut(Cookie token) throws Exception {
        return mockMvc.perform(post("/api/auth/logout").cookie(token).with(Csrf.from(mockMvc)));
    }

    private Cookie invalidTokenCookie() {
        String token = authCookieService.create(user).getValue();
        return new Cookie("mdd_token", token.substring(0, token.lastIndexOf('.') + 1) + "A".repeat(43));
    }

    private static Cookie tokenCookie(MvcResult result) {
        return MockCookie.parse(Objects.requireNonNull(result.getResponse().getHeader(HttpHeaders.SET_COOKIE)));
    }

}
