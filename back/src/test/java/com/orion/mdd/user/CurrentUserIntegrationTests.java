package com.orion.mdd.user;

import com.orion.mdd.IntegrationTest;
import com.orion.mdd.common.security.AuthCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CurrentUserIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthCookieService authCookieService;

    @Test
    void answers401ToAVisitorAndGivesTheCsrfToken() throws Exception {
        // Act
        ResultActions response = mockMvc.perform(get("/api/users/me"));

        // Assert
        response.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Vous devez être connecté."))
                .andExpect(jsonPath("$.instance").value("/api/users/me"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void refusesAForgedToken() throws Exception {
        // Arrange: a real token whose signature has been replaced
        User user = userRepository.save(new User("arthur.dent", "arthur.dent@example.com", "hash"));
        String token = authCookieService.create(user.getId()).getValue();
        String forged = token.substring(0, token.lastIndexOf('.') + 1) + "A".repeat(43);

        // Act
        ResultActions response = mockMvc.perform(get("/api/users/me").cookie(new Cookie("mdd_token", forged)));

        // Assert
        response.andExpect(status().isUnauthorized());
    }

    @Test
    void refusesATokenWhoseAccountNoLongerExists() throws Exception {
        // Act
        ResultActions response = mockMvc.perform(get("/api/users/me").with(jwt().jwt(token -> token.subject("0"))));

        // Assert
        response.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Vous devez être connecté."));
    }

}
