package com.orion.mdd.user;

import com.orion.mdd.Csrf;
import com.orion.mdd.IntegrationTest;
import com.orion.mdd.common.security.AuthCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class PasswordChangeIntegrationTests {

    private static final String PASSWORD = "Dont-Panic-42";
    private static final String NEW_PASSWORD = "So-Long-42";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthCookieService authCookieService;

    private User user;

    private Cookie token;

    @BeforeEach
    void logIn() {
        user = userRepository
                .save(new User("arthur.dent", "arthur.dent@example.com", passwordEncoder.encode(PASSWORD)));
        token = new Cookie("mdd_token", authCookieService.create(user.getId()).getValue());
    }

    @Test
    void storesTheHashOfTheNewPasswordWhenTheCurrentOneIsRight() throws Exception {
        // Act
        ResultActions response = changePassword(PASSWORD, NEW_PASSWORD);

        // Assert
        response.andExpect(status().isNoContent());
        String hash = currentHash();
        assertThat(hash).startsWith("{argon2}");
        assertThat(passwordEncoder.matches(NEW_PASSWORD, hash)).isTrue();
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isFalse();
    }

    @Test
    void refusesAWrongCurrentPassword() throws Exception {
        // Act: someone who found the session open does not know the password
        ResultActions response = changePassword("Wrong-Password-42", NEW_PASSWORD);

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currentPassword").value("Le mot de passe actuel est incorrect."));
        assertThat(passwordEncoder.matches(PASSWORD, currentHash())).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "'', So-Long-42, currentPassword",
            "Dont-Panic-42, Deep-42, newPassword"
    })
    void refusesAnInvalidField(String currentPassword, String newPassword, String invalidField) throws Exception {
        // Act: one case per field, since AccountRulesTests checks the rules
        ResultActions response = changePassword(currentPassword, newPassword);

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + invalidField).isString());
        assertThat(passwordEncoder.matches(PASSWORD, currentHash())).isTrue();
    }

    @Test
    void refusesAChangeWithoutACsrfToken() throws Exception {
        // Act: another site cannot change the password
        ResultActions response = mockMvc.perform(put("/api/users/me/password")
                .cookie(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(PASSWORD, NEW_PASSWORD)));

        // Assert
        response.andExpect(status().isForbidden());
        assertThat(passwordEncoder.matches(PASSWORD, currentHash())).isTrue();
    }

    private ResultActions changePassword(String currentPassword, String newPassword) throws Exception {
        return mockMvc.perform(put("/api/users/me/password")
                .cookie(token)
                .with(Csrf.from(mockMvc))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(currentPassword, newPassword)));
    }

    private String currentHash() {
        return userRepository.findById(user.getId()).orElseThrow().getPassword();
    }

    private static String body(String currentPassword, String newPassword) {
        return """
                {"currentPassword": "%s", "newPassword": "%s"}
                """.formatted(currentPassword, newPassword);
    }

}
