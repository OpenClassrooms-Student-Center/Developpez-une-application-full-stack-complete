package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.models.Article;
import com.openclassrooms.mddapi.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<Article> findAllDesc() {
        return this.articleRepository.findByOrderByCreatedAtDesc();
    }

    public List<Article> findAllAsc() {
        return this.articleRepository.findByOrderByCreatedAtAsc();
    }

    public Article findById(Long id) {
        return this.articleRepository.findById(id).orElse(null);
    }

    public Article addArticle(Article article) {
        return this.articleRepository.save(article);
    }
}
