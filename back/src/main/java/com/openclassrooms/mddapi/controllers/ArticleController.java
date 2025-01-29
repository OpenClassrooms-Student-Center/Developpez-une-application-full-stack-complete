package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.models.Article;
import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.services.ArticleService;
import com.openclassrooms.mddapi.services.ThemeService;
import com.openclassrooms.mddapi.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/article")
@Log4j2
public class ArticleController {

    @Autowired
    ArticleService articleService;

    @Autowired
    ThemeService themeService;

    @Autowired
    UserService userService;

    @GetMapping()
    public ResponseEntity<?> findAll() {

        // récupérer l'utilisateur
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);

        // récupérer les thèmes
        List<Theme> userThemes = user.getThemes();
        List<Long> listIds = userThemes.stream().map(t -> t.getThemeId()).collect(Collectors.toList());

        // filtrer articles en fonction des themes
        List<Article> articleList = this.articleService.findAllDesc().stream().filter(a -> listIds.contains(a.getTheme().getThemeId())).collect(Collectors.toList());
        List<ArticleDto> articleDtoList = new ArrayList<>();

        for (Article article : articleList) {
            ArticleDto articleDto = new ArticleDto();
            articleDto.setArticleId(article.getArticleId());
            articleDto.setTitre(article.getTitre());
            articleDto.setCreatedAt(article.getCreatedAt());
            articleDto.setContenu(article.getContenu());
            articleDto.setAuteur(article.getAuteur());
            articleDtoList.add(articleDto);
        }

        return ResponseEntity.ok().body(articleDtoList);
    }

    @GetMapping("/asc")
    public ResponseEntity<?> findAllAsc() {

        // récupérer l'utilisateur
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);

        // récupérer les thèmes
        List<Theme> userThemes = user.getThemes();
        List<Long> listIds = userThemes.stream().map(t -> t.getThemeId()).collect(Collectors.toList());

        // filtrer articles en fonction des themes
        List<Article> articleList = this.articleService.findAllAsc().stream().filter(a -> listIds.contains(a.getTheme().getThemeId())).collect(Collectors.toList());
        List<ArticleDto> articleDtoList = new ArrayList<>();

        for (Article article : articleList) {
            ArticleDto articleDto = new ArticleDto();
            articleDto.setArticleId(article.getArticleId());
            articleDto.setTitre(article.getTitre());
            articleDto.setCreatedAt(article.getCreatedAt());
            articleDto.setContenu(article.getContenu());
            articleDto.setAuteur(article.getAuteur());
            articleDtoList.add(articleDto);
        }

        return ResponseEntity.ok().body(articleDtoList);
    }

    @GetMapping("/{article_id}")
    public ResponseEntity<?> findById(@PathVariable("article_id") String id) {
        try {
            Article article = this.articleService.findById(Long.valueOf(id));
            if (article == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().body(article);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping()
    public ResponseEntity<?> create(@Valid @RequestBody ArticleDto articleDto) {

        Article article = new Article();
        article.setTitre(articleDto.getTitre());
        article.setContenu(articleDto.getContenu());

        Theme theme = new Theme();
        if (articleDto.getTheme_id() != null) {
            theme = themeService.findById(Long.parseLong(articleDto.getTheme_id()));
        }


        article.setTheme(theme);
        //article.setCreatedAt(articleDto.getDate());

        User user = new User();
        if (articleDto.getUser_id() != null) {
            user = userService.findById(articleDto.getUser_id());
        }
        article.setAuteur(user.getFirstName());
        article.setUser(user);
        article.setCreatedAt(new Date());

        articleService.addArticle(article);

        return ResponseEntity.ok().body(article);
    }
}
