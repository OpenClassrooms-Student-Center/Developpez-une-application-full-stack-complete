package com.orion.mdd.topic;

/**
 * A topic, as the API returns it.
 *
 * @param id
 *            the topic's identifier
 * @param title
 *            the topic's name
 * @param description
 *            what the topic covers
 */
public record TopicResponse(Long id, String title, String description) {

    public static TopicResponse from(Topic topic) {
        return new TopicResponse(topic.getId(), topic.getTitle(), topic.getDescription());
    }

}
