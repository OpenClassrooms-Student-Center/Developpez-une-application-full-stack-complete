package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.models.Article;
import com.openclassrooms.mddapi.models.Comment;
import com.openclassrooms.mddapi.models.Topic;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.repositories.ArticleRepository;
import com.openclassrooms.mddapi.repositories.CommentRepository;
import com.openclassrooms.mddapi.services.impl.ArticleService;
import com.openclassrooms.mddapi.services.impl.CommentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

    @Mock
    ArticleRepository articleRepository;


    @InjectMocks
    ArticleService articleService;
    private Article article;
    private User user;
    @BeforeEach
    void setUp() {
        user = User.builder().firstName("John").lastName("Doe").email("test@gmail.com").password("pwd").build();
        Topic topic = Topic.builder().title("Test Topic").build();
        article = Article.builder().id(1L).topic(topic).title("Tutorial JUnit").
                content("Avec Maven ou  gradle, JUnit peut être facilement ajouté à votre projet")
                .author(user).build();
    }


    @Test
    public void testSetUp() {
        Assertions.assertTrue(true);

    }

    @Test
    public void testGetArticleById() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        Article foundArticle = articleService.getArticleById(1L);
        Assertions.assertEquals(article, foundArticle);
        verify(articleRepository,times(1)).findById(1L);

    }



    @Test
    public void testGetAllArticles() {
        when(articleRepository.findAll()).thenReturn(new ArrayList<Article>());
       List<Article> foundArticles = articleService.getAllArticles();
        Assertions.assertEquals(new ArrayList<Article>(), foundArticles);
        verify(articleRepository,times(1)).findAll();

    }



}
