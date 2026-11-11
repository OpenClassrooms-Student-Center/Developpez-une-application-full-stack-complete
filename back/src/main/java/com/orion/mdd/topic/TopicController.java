package com.orion.mdd.topic;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Exposes the topics to the logged-in users.
 */
@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * Lists every topic, sorted by title, and says which ones the logged-in user follows.
     *
     * @param jwt
     *            the verified token
     * @return the topics
     */
    @GetMapping
    @ApiResponse(responseCode = "200", description = "Every topic, sorted by title, with the user's subscriptions",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = TopicResponse.class))))
    @ApiResponse(responseCode = "401", description = "Not logged in, or the account no longer exists",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    public List<TopicResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return topicService.listTopics(userId(jwt));
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }

}
