package com.orion.mdd.topic;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Reads the topics.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

}
