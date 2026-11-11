package com.orion.mdd.topic;

import com.orion.mdd.IntegrationTest;
import com.orion.mdd.common.security.AuthCookieService;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import jakarta.persistence.EntityManager;
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
import java.util.Objects;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
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

    @Autowired
    private EntityManager entityManager;

    private User user;

    private Cookie token;

    @BeforeEach
    void logIn() {
        user = userRepository.save(new User("arthur.dent", "arthur.dent@example.com", "hash"));
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

    @Test
    void marksTheTopicsTheUserFollows() throws Exception {
        // Arrange: the row is written directly, so that this test depends on no other feature. Hibernate still holds
        // the user saved above, without this row, so it must forget it, as a new request would
        Long followed = Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT MIN(id) FROM topics", Long.class));
        jdbcTemplate.update("INSERT INTO subscriptions (user_id, topic_id) VALUES (?, ?)", user.getId(), followed);
        entityManager.clear();

        // Act
        ResultActions response = mockMvc.perform(get("/api/topics").cookie(token));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)].subscribed", followed).value(true))
                .andExpect(jsonPath("$[?(@.id != %d)].subscribed", followed).value(everyItem(is(false))));
    }

}
