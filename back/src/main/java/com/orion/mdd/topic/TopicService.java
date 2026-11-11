package com.orion.mdd.topic;

import com.orion.mdd.common.exception.UserNotFoundException;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lists the topics, with the ones the logged-in user follows.
 */
@Service
public class TopicService {

    private static final Sort BY_TITLE = Sort.by("title");

    private final TopicRepository topicRepository;
    private final UserService userService;

    public TopicService(TopicRepository topicRepository, UserService userService) {
        this.topicRepository = topicRepository;
        this.userService = userService;
    }

    /**
     * Lists every topic, sorted by title, and says which ones the user follows. MySQL sorts them without regard to
     * case or accents. The transaction lets Hibernate load the subscriptions when they are first read.
     *
     * @param userId
     *            the identifier carried by the token
     * @return the topics
     * @throws UserNotFoundException
     *             if the account no longer exists
     */
    @Transactional(readOnly = true)
    public List<TopicResponse> listTopics(Long userId) {
        User user = userService.getCurrentUser(userId);
        return topicRepository.findAll(BY_TITLE).stream()
                .map(topic -> TopicResponse.from(topic, user.isSubscribedTo(topic)))
                .toList();
    }

}
