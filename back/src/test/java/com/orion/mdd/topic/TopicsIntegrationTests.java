package com.orion.mdd.topic;

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

import java.text.Collator;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class TopicsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthCookieService authCookieService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Cookie token;

    @BeforeEach
    void logIn() {
        User user = userRepository.save(new User("arthur.dent", "arthur.dent@example.com", "hash"));
        token = new Cookie("mdd_token", authCookieService.create(user.getId()).getValue());
    }

    @Test
    void listsEveryTopicSortedByTitle() throws Exception {
        // Arrange: the topics come from a migration, so the expected titles are read from the database
        List<String> titles = jdbcTemplate.queryForList("SELECT title FROM topics", String.class).stream()
                .sorted(Collator.getInstance(Locale.FRENCH))
                .toList();

        // Act
        ResultActions response = mockMvc.perform(get("/api/topics").cookie(token));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", equalTo(titles)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].description").isNotEmpty());
    }

}
