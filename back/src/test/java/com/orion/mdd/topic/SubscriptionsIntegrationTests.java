package com.orion.mdd.topic;

import com.orion.mdd.Csrf;
import com.orion.mdd.IntegrationTest;
import com.orion.mdd.common.security.AuthCookieService;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class SubscriptionsIntegrationTests {

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

    private Long topicId;

    @BeforeEach
    void logIn() {
        user = userRepository.save(new User("arthur.dent", "arthur.dent@example.com", "hash"));
        token = new Cookie("mdd_token", authCookieService.create(user.getId()).getValue());
        topicId = Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT MIN(id) FROM topics", Long.class));
    }

    @Test
    void subscribesTheUserToTheTopic() throws Exception {
        // Act
        ResultActions response = subscribe(topicId);

        // Assert
        response.andExpect(status().isNoContent());
        assertThat(subscriptionRows()).isOne();
    }

    @Test
    void changesNothingWhenTheUserAlreadyFollowsTheTopic() throws Exception {
        // Arrange
        subscribe(topicId);

        // Act
        ResultActions response = subscribe(topicId);

        // Assert
        response.andExpect(status().isNoContent());
        assertThat(subscriptionRows()).isOne();
    }

    @Test
    void refusesATopicThatDoesNotExist() throws Exception {
        // Act
        ResultActions response = subscribe(0L);

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ce thème n’existe pas."));
    }

    private ResultActions subscribe(Long id) throws Exception {
        return mockMvc.perform(put("/api/topics/{id}/subscription", id).cookie(token).with(Csrf.from(mockMvc)));
    }

    // Flush writes now what the end of a real request would write, since the test transaction never commits
    private Integer subscriptionRows() {
        userRepository.flush();
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subscriptions WHERE user_id = ? AND topic_id = ?",
                Integer.class, user.getId(), topicId);
    }

}
