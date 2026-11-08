package com.orion.mdd.auth;

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

import java.util.Objects;
import java.util.stream.Stream;

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

    /** Sends what a browser sends: a first call gives the CSRF cookie, then the POST repeats it in a header. */
    private ResultActions logIn(String email, String password, Cookie... otherCookies) throws Exception {
        Cookie csrfCookie = csrfCookie();
        return mockMvc.perform(post("/api/auth/login")
                .cookie(Stream.concat(Stream.of(csrfCookie), Stream.of(otherCookies)).toArray(Cookie[]::new))
                .header("X-XSRF-TOKEN", csrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)));
    }

    private Cookie csrfCookie() throws Exception {
        return Objects.requireNonNull(
                mockMvc.perform(get("/api/users/me")).andReturn().getResponse().getCookie("XSRF-TOKEN"));
    }

    private Cookie invalidTokenCookie() {
        String token = authCookieService.create(user).getValue();
        return new Cookie("mdd_token", token.substring(0, token.lastIndexOf('.') + 1) + "A".repeat(43));
    }

    private static Cookie tokenCookie(MvcResult result) {
        return MockCookie.parse(Objects.requireNonNull(result.getResponse().getHeader(HttpHeaders.SET_COOKIE)));
    }

}
