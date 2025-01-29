package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.models.Article;
import com.openclassrooms.mddapi.models.Comment;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.services.ArticleService;
import com.openclassrooms.mddapi.services.CommentService;
import com.openclassrooms.mddapi.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comment")
@Log4j2
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    ArticleService articleService;

    @Autowired
    UserService userService;

    @PostMapping()
    public ResponseEntity<?> create(@Valid @RequestBody CommentDto comment) {

        Article article = new Article();
        if(comment.getArticleId() != null) {
            article = articleService.findById(Long.parseLong(comment.getArticleId()));
        }

        User user = new User();
        if(comment.getUserId() != null) {
            user = userService.findById(Long.parseLong(comment.getUserId()));
        }
        Comment newComment = new Comment();
        newComment.setCommentaire(comment.getCommentaire());
        newComment.setArticle(article);
        newComment.setUser(user);
        newComment.setAuteur(user.getFirstName());

        commentService.addComment(newComment);

        return ResponseEntity.ok().body(newComment);
    }
    }
