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
 * @param subscribed
 *            whether the logged-in user follows it
 */
public record TopicResponse(Long id, String title, String description, boolean subscribed) {

    public static TopicResponse from(Topic topic, boolean subscribed) {
        return new TopicResponse(topic.getId(), topic.getTitle(), topic.getDescription(), subscribed);
    }

}
