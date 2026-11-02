package com.orion.mdd.auth;

import com.orion.mdd.IntegrationTest;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

}
