package com.orion.mdd.topic;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lists the topics.
 */
@Service
public class TopicService {

    private static final Sort BY_TITLE = Sort.by("title");

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    /**
     * Lists every topic, sorted by title. MySQL sorts them without regard to case or accents.
     *
     * @return the topics
     */
    public List<TopicResponse> listTopics() {
        return topicRepository.findAll(BY_TITLE).stream().map(TopicResponse::from).toList();
    }

}
