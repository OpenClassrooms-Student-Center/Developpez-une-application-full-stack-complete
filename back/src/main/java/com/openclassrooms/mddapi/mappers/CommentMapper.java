package com.openclassrooms.mddapi.mappers;

import com.openclassrooms.mddapi.dtos.CommentDto;
import com.openclassrooms.mddapi.dtos.MddUserDto;
import com.openclassrooms.mddapi.model.Comment;
import com.openclassrooms.mddapi.model.MddUser;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.service.CommentService;
import com.openclassrooms.mddapi.service.MddUserService;
import com.openclassrooms.mddapi.service.PostService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Mapper(componentModel = "spring", uses = {CommentService.class}, imports = {Arrays.class, Collectors.class, Comment.class, Post.class, MddUser.class, Collections.class, Optional.class})
public abstract class CommentMapper implements EntityMapper<CommentDto, Comment> {

    @Autowired
    protected MddUserService mddUserService;

    @Autowired
    protected PostService postService;


    @Mappings({
            @Mapping(target = "author", expression = "java(this.mddUserService.findUserById(commentDto.getAuthorId()))"),
            @Mapping(target = "post", expression = "java(this.postService.findPostById(commentDto.getPostId()))"),
    })
    public abstract Comment toEntity(CommentDto commentDto);
    @Mappings({
            @Mapping(target = "postId", source = "post.id"),
            @Mapping(target = "authorId", source = "author.id"),
    })
    public abstract CommentDto toDto(Comment comment);

}
