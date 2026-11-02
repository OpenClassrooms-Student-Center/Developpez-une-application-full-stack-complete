package com.orion.mdd.auth;

import com.orion.mdd.IntegrationTest;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

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

    /** Sends what a browser sends: a first call gives the CSRF cookie, then the POST repeats it in a header. */
    private ResultActions register(String username, String email, String password) throws Exception {
        Cookie csrfCookie = mockMvc.perform(get("/api/users/me")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        return mockMvc.perform(post("/api/auth/register")
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(username, email, password)));
    }

    private static String body(String username, String email, String password) {
        return """
                {"username": "%s", "email": "%s", "password": "%s"}
                """.formatted(username, email, password);
    }

    private static Cookie tokenCookie(MvcResult result) {
        return MockCookie.parse(result.getResponse().getHeader(HttpHeaders.SET_COOKIE));
    }

}
