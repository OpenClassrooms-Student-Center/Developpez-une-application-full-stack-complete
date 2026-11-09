package com.orion.mdd.user;

import com.orion.mdd.Csrf;
import com.orion.mdd.IntegrationTest;
import com.orion.mdd.auth.AuthCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ProfileIntegrationTests {

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

    private Cookie token;

    @BeforeEach
    void logIn() {
        user = userRepository
                .save(new User("arthur.dent", "arthur.dent@example.com", passwordEncoder.encode(PASSWORD)));
        token = new Cookie("mdd_token", authCookieService.create(user).getValue());
    }

    @Test
    void changesTheUsernameAndTheEmail() throws Exception {
        // Act
        ResultActions response = updateProfile("ford.prefect", "ford.prefect@example.com");

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("ford.prefect"))
                .andExpect(jsonPath("$.email").value("ford.prefect@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        User saved = userRepository.findById(user.getId()).orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("ford.prefect");
        assertThat(saved.getEmail()).isEqualTo("ford.prefect@example.com");
    }

    @Test
    void neverChangesThePassword() throws Exception {
        // Act: the password has its own form, which asks for the current one
        mockMvc.perform(put("/api/users/me")
                .cookie(token)
                .with(Csrf.from(mockMvc))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "arthur.dent", "email": "arthur.dent@example.com", "password": "So-Long-42"}
                        """))
                .andExpect(status().isOk());

        // Assert
        String hash = userRepository.findById(user.getId()).orElseThrow().getPassword();
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();
    }

    @Test
    void keepsTheUserLoggedInAfterAChange() throws Exception {
        // Act: the token names the account by its id, which never changes
        updateProfile("ford.prefect", "ford.prefect@example.com");

        // Assert
        mockMvc.perform(get("/api/users/me").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ford.prefect"));
    }

    @Test
    void refusesAUsernameTakenByAnotherUserWhateverItsCase() throws Exception {
        // Arrange
        userRepository.save(new User("ford.prefect", "ford.prefect@example.com", "hash"));

        // Act
        ResultActions response = updateProfile("Ford.Prefect", "arthur.dent@example.com");

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.username").value("Ce nom d’utilisateur est déjà pris."));
    }

    @Test
    void refusesAnEmailTakenByAnotherUserWhateverItsCase() throws Exception {
        // Arrange
        userRepository.save(new User("ford.prefect", "ford.prefect@example.com", "hash"));

        // Act
        ResultActions response = updateProfile("arthur.dent", "FORD.PREFECT@example.com");

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("Cette adresse e-mail est déjà utilisée."));
    }

    @Test
    void refusesATokenWhoseAccountNoLongerExistsEvenWithATakenUsername() throws Exception {
        // Arrange: the account is deleted, and its token asks for the username of another user
        userRepository.save(new User("ford.prefect", "ford.prefect@example.com", "hash"));
        userRepository.delete(user);
        userRepository.flush();

        // Act
        ResultActions response = updateProfile("ford.prefect", "arthur.dent@example.com");

        // Assert: the account is checked before the values it asks for
        response.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Vous devez être connecté."));
    }

    @Test
    void acceptsTheUsersOwnValuesEvenInAnotherCase() throws Exception {
        // Act: the user's own username and e-mail address are not taken by someone else
        ResultActions response = updateProfile("Arthur.Dent", "ARTHUR.DENT@example.com");

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Arthur.Dent"));
    }

    @Test
    void refusesAChangeWithoutACsrfToken() throws Exception {
        // Act: another site cannot change the profile
        ResultActions response = mockMvc.perform(put("/api/users/me")
                .cookie(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("ford.prefect", "ford.prefect@example.com")));

        // Assert
        response.andExpect(status().isForbidden());
        assertThat(userRepository.findById(user.getId()).orElseThrow().getUsername()).isEqualTo("arthur.dent");
    }

    private ResultActions updateProfile(String username, String email) throws Exception {
        return mockMvc.perform(put("/api/users/me")
                .cookie(token)
                .with(Csrf.from(mockMvc))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(username, email)));
    }

    private static String body(String username, String email) {
        return """
                {"username": "%s", "email": "%s"}
                """.formatted(username, email);
    }

}
