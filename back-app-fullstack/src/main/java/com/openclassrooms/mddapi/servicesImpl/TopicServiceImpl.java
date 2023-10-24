package com.openclassrooms.mddapi.servicesImpl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.models.Post;
import com.openclassrooms.mddapi.models.Topic;
import com.openclassrooms.mddapi.repositories.TopicRepository;
import com.openclassrooms.mddapi.services.TopicService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicRepository topicRepository;

    // @Override
    // public List<TopicDto> getAllTopics() {
    // List<Topic> topics = topicRepository.findAll();

    // List<TopicDto> topicsDto = topics.stream().map(topic ->
    // modelMapper.map(topic, TopicDto.class))
    // .collect(Collectors.toList());
    // return topicsDto;
    // }

    // @Override
    // public Topic findById(Long id) {
    // return this.topicRepository.findById(id).get();

    // }

    @Override
    public List<Topic> getAllTopics() {
        return this.topicRepository.findAll();
    }

    @Override
    public Topic findById(Long id) {
        return this.topicRepository.findById(id).get();
    }

}
