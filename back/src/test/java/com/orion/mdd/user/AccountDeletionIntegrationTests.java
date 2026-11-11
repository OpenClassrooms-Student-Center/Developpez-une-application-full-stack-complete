package com.orion.mdd.user;

import com.orion.mdd.Csrf;
import com.orion.mdd.IntegrationTest;
import com.orion.mdd.common.security.AuthCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AccountDeletionIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthCookieService authCookieService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;

    private Cookie token;

    @BeforeEach
    void logIn() {
        user = userRepository.save(new User("arthur.dent", "arthur.dent@example.com", "hash"));
        token = new Cookie("mdd_token", authCookieService.create(user.getId()).getValue());
    }

    @Test
    void deletesTheAccountAndClearsTheTokenCookie() throws Exception {
        // Act
        ResultActions response = deleteAccount();

        // Assert
        response.andExpect(status().isNoContent());
        assertThat(response.andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE))
                .startsWith("mdd_token=;")
                .contains("Max-Age=0");
        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    @Test
    void leavesTheTokenOfTheDeletedAccountUseless() throws Exception {
        // Act: a copy of the cookie kept elsewhere is still valid for 24 hours
        deleteAccount();

        // Assert
        mockMvc.perform(get("/api/users/me").cookie(token)).andExpect(status().isUnauthorized());
    }

    @Test
    void deletesTheSubscriptionsOfTheAccount() throws Exception {
        // Arrange: the row is written directly, so that this test depends on no other feature
        jdbcTemplate.update("INSERT INTO subscriptions (user_id, topic_id) SELECT ?, id FROM topics LIMIT 1",
                user.getId());

        // Act: flush sends the DELETE now, since the test transaction never commits
        deleteAccount();
        userRepository.flush();

        // Assert: the database deletes them in cascade
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subscriptions WHERE user_id = ?",
                Integer.class, user.getId())).isZero();
    }

    @Test
    void refusesADeletionWithoutACsrfToken() throws Exception {
        // Act: another site cannot delete the account
        ResultActions response = mockMvc.perform(delete("/api/users/me").cookie(token));

        // Assert
        response.andExpect(status().isForbidden());
        assertThat(userRepository.existsById(user.getId())).isTrue();
    }

    private ResultActions deleteAccount() throws Exception {
        return mockMvc.perform(delete("/api/users/me").cookie(token).with(Csrf.from(mockMvc)));
    }

}
