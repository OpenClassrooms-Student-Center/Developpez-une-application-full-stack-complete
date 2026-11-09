package com.orion.mdd.auth;

import com.orion.mdd.Csrf;
import com.orion.mdd.IntegrationTest;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class RegistrationIntegrationTests {

    private static final String PASSWORD = "Dont-Panic-42";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsTheAccountAndReturnsTheProfile() throws Exception {
        // Act
        ResultActions response = register("arthur.dent", "arthur.dent@example.com", PASSWORD);

        // Assert
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("arthur.dent"))
                .andExpect(jsonPath("$.email").value("arthur.dent@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void storesTheHashOfThePassword() throws Exception {
        // Act
        register("arthur.dent", "arthur.dent@example.com", PASSWORD);

        // Assert
        User user = userRepository.findAll().getFirst();
        assertThat(user.getPassword()).startsWith("{argon2}$argon2id$v=19$m=19456,t=2,p=1$");
        assertThat(passwordEncoder.matches(PASSWORD, user.getPassword())).isTrue();
    }

    @Test
    void logsTheNewUserIn() throws Exception {
        // Act
        MvcResult result = register("arthur.dent", "arthur.dent@example.com", PASSWORD).andReturn();

        // Assert
        String cookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(cookie).startsWith("mdd_token=").contains("Max-Age=86400", "Path=/", "Secure", "HttpOnly",
                "SameSite=Strict");
        mockMvc.perform(get("/api/users/me").cookie(tokenCookie(result)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("arthur.dent"));
    }

    @Test
    void refusesATakenUsernameWhateverItsCase() throws Exception {
        // Arrange
        register("arthur.dent", "arthur.dent@example.com", PASSWORD);

        // Act
        ResultActions response = register("Arthur.Dent", "ford.prefect@example.com", PASSWORD);

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Ce nom d’utilisateur est déjà pris."))
                .andExpect(jsonPath("$.errors.username").value("Ce nom d’utilisateur est déjà pris."))
                .andExpect(jsonPath("$.instance").value("/api/auth/register"));
    }

    @Test
    void refusesATakenEmailWhateverItsCase() throws Exception {
        // Arrange
        register("arthur.dent", "arthur.dent@example.com", PASSWORD);

        // Act
        ResultActions response = register("ford.prefect", "ARTHUR.DENT@example.com", PASSWORD);

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("Cette adresse e-mail est déjà utilisée."));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void refusesAnInvalidField(String username, String email, String password, String invalidField) throws Exception {
        // Act
        ResultActions response = register(username, email, password);

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors." + invalidField).isString());
        assertThat(userRepository.count()).isZero();
    }

    private static Stream<Arguments> invalidFields() {
        String valid = "arthur.dent";
        String email = "arthur.dent@example.com";
        return Stream.of(
                Arguments.of("   ", email, PASSWORD, "username"),
                Arguments.of("42", email, PASSWORD, "username"),
                Arguments.of("zaphod.beeblebrox.the.president", email, PASSWORD, "username"),
                Arguments.of("arthur.dent@home", email, PASSWORD, "username"),
                Arguments.of("marvin.déprimé", email, PASSWORD, "username"),
                Arguments.of(valid, "   ", PASSWORD, "email"),
                Arguments.of(valid, "arthur.dent", PASSWORD, "email"),
                Arguments.of(valid, "dent@" + "vogon.".repeat(40) + "example.com", PASSWORD, "email"),
                Arguments.of(valid, email, "   ", "password"),
                Arguments.of(valid, email, "Deep-42", "password"),
                Arguments.of(valid, email, "Dont-Panic-42".repeat(5), "password"),
                Arguments.of(valid, email, "dont-panic-42", "password"),
                Arguments.of(valid, email, "DONT-PANIC-42", "password"),
                Arguments.of(valid, email, "Dont-Panic", "password"),
                Arguments.of(valid, email, "DontPanic42", "password"));
    }

    @Test
    void refusesARequestWithoutACsrfToken() throws Exception {
        // Act
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("arthur.dent", "arthur.dent@example.com", PASSWORD)));

        // Assert
        response.andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Cette requête a été refusée."));
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void refusesARequestWithoutACsrfTokenEvenFromALoggedInUser() throws Exception {
        // Arrange
        Cookie token = tokenCookie(register("arthur.dent", "arthur.dent@example.com", PASSWORD).andReturn());

        // Act
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .cookie(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("ford.prefect", "ford.prefect@example.com", PASSWORD)));

        // Assert
        response.andExpect(status().isForbidden());
    }

    /** Sends what a browser sends: a first call gives the CSRF cookie, then the POST repeats it in a header. */
    private ResultActions register(String username, String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .with(Csrf.from(mockMvc))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(username, email, password)));
    }

    private static String body(String username, String email, String password) {
        return """
                {"username": "%s", "email": "%s", "password": "%s"}
                """.formatted(username, email, password);
    }

    private static Cookie tokenCookie(MvcResult result) {
        return MockCookie.parse(Objects.requireNonNull(result.getResponse().getHeader(HttpHeaders.SET_COOKIE)));
    }

}
